package ru.kanban.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.dao.*;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.kanban.model.Status.IN_PROGRESS;
import static ru.kanban.model.Status.NEW;

class HistoryServiceImplTest {
    private static Connection connection;

    private HistoryService historyService;
    private HistoryDao historyManager;
    private TaskDao taskDao;

    private Task task1;
    private Task task2;
    private Task task3;

    private Epic epic1;
    private Epic epic2;

    private Subtask subtask1;

    @BeforeAll
    static void initConnection() throws IOException {
        try (InputStream in = DbHistoryDao.class.getClassLoader().getResourceAsStream(
                "db/test.properties"
        )) {
            Properties config = new Properties();
            config.load(in);
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void wipeTable() {
        try (PreparedStatement statement = connection.prepareStatement(
                "delete from history"
        )) {
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
        historyManager = new DbHistoryDao(connection);
        historyService = new HistoryServiceImpl(historyManager);
        taskDao = new DbTaskDao(connection);
        task1 = new Task("task1", "desc", NEW);
        task2 = new Task("task2", "desc", IN_PROGRESS);
        task3 = new Task("task3", "desc", NEW);
        epic1 = new Epic("epic1", "desc", NEW);
        epic2 = new Epic("epic2", "desc", NEW);
        subtask1 = new Subtask("subtask1", "desc", NEW, epic1);
    }

    @Test
    void whenAddNullTaskToHistoryThenHistoryDoesntContainsTask() {
        historyService.addToHistory(null);
        assertThat(historyService.getViewedTasks()).isEmpty();
    }

    @Test
    void whenAddTaskWithInMemoryHistoryDaoThenGetViewedTaskContainsTask() {
        historyManager = new InMemoryHistoryDao();
        historyService = new HistoryServiceImpl(historyManager);
        historyService.addToHistory(task1);
        assertThat(historyService.getViewedTasks()).contains(task1);
    }

    @Test
    void whenAddTaskWithFileBackedHistoryDaoThenGetViewedTaskContainsTask() throws IOException {
        File tempFile = File.createTempFile("temp", ".csv");
        historyManager = new FileBackedHistoryDao(tempFile.toString());
        historyService = new HistoryServiceImpl(historyManager);
        historyService.addToHistory(task1);
        assertThat(historyService.getViewedTasks()).contains(task1);
    }

    @Test
    void whenAddTaskWithDBHistoryDaoThenGetViewedTaskContainsTask() throws IOException {
        taskDao.addTask(task1);
        historyService.addToHistory(task1);
        assertThat(historyService.getViewedTasks()).contains(task1);
    }

    @Test
    void whenAddTaskToHistoryButTaskDaoDoesntContainsTaskExceptionThrown() {
        assertThatThrownBy(() -> historyService.addToHistory(task1))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void whenSetToViewedThenTaskIsViewed() {
        historyService.setToViewed(task1);
        assertThat(task1.isViewed()).isTrue();
    }

    @Test
    void whenRemoveThenHistoryDoesntContainsTask() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addTask(task3);
        historyService.addToHistory(task1);
        historyService.addToHistory(task2);
        historyService.addToHistory(task3);
        historyService.remove(task1.getId());
        assertThat(historyService.getViewedTasks()).doesNotContain(task1);
    }

    @Test
    void whenRemoveWithInMemoryHistoryDaoThenHistoryDoesntContainsTask() {
        historyManager = new InMemoryHistoryDao();
        historyService = new HistoryServiceImpl(historyManager);
        historyService.addToHistory(task1);
        historyService.addToHistory(task2);
        historyService.addToHistory(task3);
        historyService.remove(task1.getId());
        assertThat(historyService.getViewedTasks()).doesNotContain(task1);
    }

    @Test
    void whenRemoveWithFileBackedHistoryDaoThenHistoryDoesntContainsTask() throws IOException {
        File tempFile = File.createTempFile("temp", ".csv");
        historyManager = new FileBackedHistoryDao(tempFile.toString());
        historyService = new HistoryServiceImpl(historyManager);
        historyService.addToHistory(task1);
        historyService.addToHistory(task2);
        historyService.addToHistory(task3);
        historyService.remove(task1.getId());
        assertThat(historyService.getViewedTasks()).doesNotContain(task1);
    }

}