package ru.kanban.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.*;
import ru.kanban.dao.DbHistoryDao;
import ru.kanban.dao.DbTaskDao;
import ru.kanban.dao.TaskDao;
import ru.kanban.exceptions.DaoException;
import ru.kanban.validator.TaskValidator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DBTaskServiceTest extends TaskServiceTest {
    private static Connection connection;

    @BeforeAll
    static void initConnection() throws IOException {
        try (InputStream in = TaskService.class.getClassLoader().getResourceAsStream("db/test.properties")) {
            Properties config = new Properties();
            config.load(in);
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void wipeTable() throws SQLException {
        try (PreparedStatement deleteFromTasks = connection.prepareStatement(
                "delete from tasks"
        );
             PreparedStatement deleteFromHistory = connection.prepareStatement(
                     "delete from history"
             )) {
            deleteFromTasks.execute();
            deleteFromHistory.execute();
        }
    }

    @AfterAll
    static void close() throws SQLException {
        connection.close();
    }

    @Override
    TaskService createService() {
        TaskDao taskDao = new DbTaskDao(connection);
        HistoryService historyService = new HistoryServiceImpl(new DbHistoryDao(connection));
        return new TaskServiceImpl(taskDao, historyService, new TaskValidator());
    }

    @Test
    void whenConnectionClosedThenDaoExceptionThrown() throws SQLException, IOException {
        taskService.addTask(task1);
        connection.close();

        assertThatThrownBy(() -> taskService.getTask(task1.getId()))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.getTasks())
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteTask(1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.updateTask(task1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteAllTasks())
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.getEpic(1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.getEpics())
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteEpic(1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.updateEpic(epic1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteEpic(1))
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteAllEpics())
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.getSubtask(1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.getSubtasks())
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteSubtask(1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.updateSubtask(subtask1))
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteSubtask(1))
                .hasMessageContaining("Database connection failure: ");

        assertThatThrownBy(() -> taskService.deleteAllSubtasks())
                .isInstanceOf(DaoException.class)
                .hasMessageContaining("Database connection failure: ");

        initConnection();
    }
}
