package ru.kanban.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.*;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.kanban.model.Status.NEW;

class DbHistoryDaoTest extends HistoryDaoTest {
    private static Connection connection;
    protected DbTaskDao taskDao;

    @Override
    HistoryDao createHistoryDao() {
        taskDao = new DbTaskDao(connection);

        return new DbHistoryDao(connection);
    }

    @BeforeAll
    static void initConnection() {
        try (InputStream in = DbHistoryDao.class.getClassLoader().getResourceAsStream(
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

    @BeforeEach
    void addTasks() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addTask(task3);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.addSubtask(subtask1);
        taskDao.addSubtask(subtask2);
    }

    @AfterEach
    void wipeTable() {
        try (PreparedStatement deleteFromHistory = connection.prepareStatement(
                "delete from history");
             PreparedStatement deleteFromTasks = connection.prepareStatement(
                     "delete from tasks")) {
            deleteFromHistory.execute();
            deleteFromTasks.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void close() throws SQLException {
        connection.close();

    }

    @Override
    @Test
    void whenAddMoreThan10TasksThenHistoryDoesntContainsFirstViewedWillAbsent() {
        historyDao.addToHistory(task1);
        historyDao.addToHistory(task2);
        historyDao.addToHistory(task3);
        for (int i = 4; i <= 11; i++) {
            Task lastTask = new Task("task" + i, "desc", NEW);
            taskDao.addTask(lastTask);
            historyDao.addToHistory(lastTask);
        }
        assertThat(historyDao.getViewedTasks()).hasSize(10)
                .doesNotContain(task1)
                .contains(task2, task3);
    }

    @Override
    @Test
    void whenAddAllTypesOfTaskThenHistoryContainsAll() {
        historyDao.addToHistory(task1);
        historyDao.addToHistory(epic1);
        historyDao.addToHistory(subtask1);
        assertThat(historyDao.getViewedTasks()).containsExactly(task1, epic1, subtask1);
    }
}