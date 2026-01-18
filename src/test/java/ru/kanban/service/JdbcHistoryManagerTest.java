package ru.kanban.service;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.*;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.kanban.model.Status.IN_PROGRESS;
import static ru.kanban.model.Status.NEW;

class JdbcHistoryManagerTest {
    private static Connection connection;

    private JdbcManager manager;
    private HistoryManager historyManager;

    private Task task1;
    private Task task2;
    private Task task3;

    private Epic epic1;
    private Epic epic2;

    private Subtask subtask1;

    @BeforeAll
    static void initConnection() {
        try (InputStream in = JdbcHistoryManager.class.getClassLoader().getResourceAsStream(
                "db/test.properties")) {
            Properties config = new Properties();
            config.load(in);
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void wipeTable() {
        try (PreparedStatement statement = connection.prepareStatement(
                "delete from history")) {
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void close() throws SQLException {
        connection.close();

    }

    @BeforeEach
    void setUp() {
        historyManager = new JdbcHistoryManager(connection);
        manager = new JdbcManager(connection, historyManager);
        task1 = new Task("task1", "desc", NEW);
        task2 = new Task("task2", "desc", IN_PROGRESS);
        task3 = new Task("task3", "desc", NEW);
        epic1 = new Epic("epic1", "desc", NEW);
        epic2 = new Epic("epic2", "desc", NEW);
        subtask1 = new Subtask("subtask1", "desc", NEW, epic1);
    }

    @Test
    void whenAddTaskHistoryContainsTask() {
        manager.addTask(task1);
        historyManager.addToHistory(task1);
        assertThat(historyManager.getViewedTasks()).containsOnly(task1);
    }

    @Test
    void whenAddEpicThenHistoryContainsEpic() {
        manager.addEpic(epic1);
        historyManager.addToHistory(epic1);
        assertThat(historyManager.getViewedTasks()).containsOnly(epic1);
    }

    @Test
    void whenAddSubtaskThenHistoryContainsEpic() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        historyManager.addToHistory(subtask1);
        assertThat(historyManager.getViewedTasks()).containsOnly(subtask1);
    }

    @Test
    void whenAddAllTypesOfTaskThenHistoryContainsAll() {
        manager.addTask(task1);
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        historyManager.addToHistory(task1);
        historyManager.addToHistory(epic1);
        historyManager.addToHistory(subtask1);
        assertThat(historyManager.getViewedTasks()).containsExactly(task1, epic1, subtask1);
    }

    @Test
    void whenAddMoreThan10TasksThenHistoryDoesntContainsFirstViewedWillAbsent() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        historyManager.addToHistory(task1);
        historyManager.addToHistory(task2);
        historyManager.addToHistory(task3);
        for (int i = 4; i <= 11; i++) {
            Task lastTask = new Task("task" + i, "desc", NEW);
            manager.addTask(lastTask);
            historyManager.addToHistory(lastTask);
        }
        assertThat(historyManager.getViewedTasks()).hasSize(10)
                .doesNotContain(task1)
                .contains(task2, task3);
    }

    @Test
    void whenSetToViewedThenTaskUpdated() {
        manager.addTask(task1);
        historyManager.addToHistory(task1);
        historyManager.setToViewed(task1);
        assertThat(historyManager.getViewedTasks().get(0).isViewed()).isTrue();
    }

    @Test
    void whenRemoveThenHistoryDoesntContainsTask() {
        manager.addTask(task1);
        historyManager.addToHistory(task1);
        historyManager.remove(task1.getId());
        assertThat(historyManager.getViewedTasks()).doesNotContain(task1);
    }
}