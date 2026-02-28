package ru.kanban.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.*;

class DbTaskDaoTest extends DaoTest {
    private static Connection connection;

    @Override
    TaskDao createDao() {
        return new DbTaskDao(connection);
    }

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = DbTaskDao.class.getClassLoader().getResourceAsStream("db/test.properties")) {
            Properties config = new Properties();
            config.load(in);
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @BeforeEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement deleteHistory = connection.prepareStatement("DELETE from history");
             PreparedStatement deleteTasks = connection.prepareStatement("DELETE from tasks")) {
            deleteHistory.execute();
            deleteTasks.execute();
        }
    }

    @Test
    @Disabled
    void whenDeleteAllEpicsHistoryDoesntContainEpicsAndSubtasks() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addTask(task3);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.getTask(task1.getId());
        taskDao.getTask(task2.getId());
        taskDao.getTask(task3.getId());
        taskDao.getEpic(epic1.getId());
        taskDao.getEpic(epic2.getId());
        taskDao.deleteAllEpics();
    }

    @Test
    @Disabled
    void whenDeleteAllSubtasksHistoryDoesntContainSubtasks() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addTask(task3);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.getTask(task1.getId());
        taskDao.getTask(task2.getId());
        taskDao.getTask(task3.getId());
        taskDao.getEpic(epic1.getId());
        taskDao.getEpic(epic2.getId());
        taskDao.deleteAllSubtasks();
    }
}