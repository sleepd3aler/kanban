package ru.kanban.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.*;
import ru.kanban.dao.*;
import ru.kanban.exceptions.DaoException;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
import ru.kanban.utils.Managers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.kanban.model.Status.*;

class TaskServiceImplTest {
    private static Connection connection;
    private HistoryService historyService;
    private TaskService taskService;

    private File tempFile;

    private HistoryDao historyDao;
    private TaskDao taskDao;

    private Task task1;
    private Task task2;
    private Task task3;

    private Epic epic1;
    private Epic epic2;

    private Subtask subtask1;
    private Subtask subtask2;

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

    @AfterEach
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

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        historyDao = new DbHistoryDao(connection);
        taskDao = new DbTaskDao(connection);
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        task1 = new Task("task1", "desc", NEW);
        task2 = new Task("task2", "desc", IN_PROGRESS);
        task3 = new Task("task3", "desc", NEW);
        epic1 = new Epic("epic1", "desc", NEW);
        epic2 = new Epic("epic2", "desc", NEW);
        subtask1 = new Subtask("subtask1", "desc", NEW, epic1);
        subtask2 = new Subtask("subtask2", "desc", NEW, epic2);
    }

    @AfterAll
    static void close() throws SQLException {
        connection.close();
    }

    @Test
    void whenWithDbDaoAddAnyTasksThenServiceContainsTask() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        assertThat(taskService.getTasks()).hasSize(3).containsExactly(task1, task2, task3);
        assertThat(taskService.getEpics()).hasSize(2).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).hasSize(2).containsExactly(subtask1, subtask2);
    }

    @Test
    void whenWithInMemDaoAddAnyTasksThenServiceContainsTask() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        assertThat(taskService.getTasks()).hasSize(3).containsExactly(task1, task2, task3);
        assertThat(taskService.getEpics()).hasSize(2).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).hasSize(2).containsExactly(subtask1, subtask2);
    }

    @Test
    void whenWithFileBackedDaoAddAnyTasksThenServiceContainsTask() throws IOException {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = new FileBackedTaskDao(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        assertThat(taskService.getTasks()).hasSize(3).containsExactly(task1, task2, task3);
        assertThat(taskService.getEpics()).hasSize(2).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).hasSize(2).containsExactly(subtask1, subtask2);
    }

    @Test
    void whenTryAddNullTasksThenIllegalArgumentExceptionThrown() {
        assertThatThrownBy(() -> taskService.addTask(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not null required");
        assertThatThrownBy(() -> taskService.addEpic(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic not null required");
        assertThatThrownBy(() -> taskService.addSubtask(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subtask not null required");
    }

    @Test
    void whenTryAddSubtaskAndServiceDoesntContainsEpicThenTaskNotFoundExceptionThrown() {
        assertThatThrownBy(() -> taskService.addSubtask(subtask1)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Epic with id: " + subtask1.getEpic().getId() + " not found");
    }

    @Test
    void whenGetAnyTypeTaskWithDbDaoThenExpectedResult() {
        taskService.addTask(task1);
        taskService.addEpic(epic1);
        taskService.addSubtask(subtask1);
        Task expected = task1;
        Epic expectedEpic = epic1;
        Subtask expectedSubtask = subtask1;
        assertThat(taskService.getTask(task1.getId()).get()).isEqualTo(expected);
        assertThat(taskService.getEpic(epic1.getId()).get()).isEqualTo(expectedEpic);
        assertThat(taskService.getSubtask(subtask1.getId()).get()).isEqualTo(expectedSubtask);
        assertThat(taskService.getTask(task1.getId()).get().isViewed()).isTrue();
        assertThat(taskService.getEpic(epic1.getId()).get().isViewed()).isTrue();
        assertThat(taskService.getSubtask(subtask1.getId()).get().isViewed()).isTrue();
    }

    @Test
    void whenGetAnyTypeTaskWithInMemDaoThenExpectedResult() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);

        taskService.addTask(task1);
        taskService.addEpic(epic1);
        taskService.addSubtask(subtask1);
        Task expected = task1;
        Epic expectedEpic = epic1;
        Subtask expectedSubtask = subtask1;
        assertThat(taskService.getTask(task1.getId()).get()).isEqualTo(expected);
        assertThat(taskService.getEpic(epic1.getId()).get()).isEqualTo(expectedEpic);
        assertThat(taskService.getSubtask(subtask1.getId()).get()).isEqualTo(expectedSubtask);
        assertThat(taskService.getTask(task1.getId()).get().isViewed()).isTrue();
        assertThat(taskService.getEpic(epic1.getId()).get().isViewed()).isTrue();
        assertThat(taskService.getSubtask(subtask1.getId()).get().isViewed()).isTrue();
    }

    @Test
    void whenGetAnyTypeTaskWithFileBackedDaoThenExpectedResult() {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = new FileBackedTaskDao(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);

        taskService.addTask(task1);
        taskService.addEpic(epic1);
        taskService.addSubtask(subtask1);
        Task expected = task1;
        Epic expectedEpic = epic1;
        Subtask expectedSubtask = subtask1;
        assertThat(taskService.getTask(task1.getId()).get()).isEqualTo(expected);
        assertThat(taskService.getEpic(epic1.getId()).get()).isEqualTo(expectedEpic);
        assertThat(taskService.getSubtask(subtask1.getId()).get()).isEqualTo(expectedSubtask);
        assertThat(taskService.getTask(task1.getId()).get().isViewed()).isTrue();
        assertThat(taskService.getEpic(epic1.getId()).get().isViewed()).isTrue();
        assertThat(taskService.getSubtask(subtask1.getId()).get().isViewed()).isTrue();
    }

    @Test
    void whenGetTasksWIthDbDaoThenHistoryContainsTasksAndTasksIsViewed() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Task> tasks = taskService.getTasks();
        for (Task task : tasks) {
            assertThat(task.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(task1, task2, task3)
                .doesNotContain(epic1, epic2, subtask1, subtask2);
    }

    @Test
    void whenGetTasksWIthInMemDaoThenHistoryContainsTasksAndTasksIsViewed() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Task> tasks = taskService.getTasks();
        for (Task task : tasks) {
            assertThat(task.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(task1, task2, task3)
                .doesNotContain(epic1, epic2, subtask1, subtask2);
    }

    @Test
    void whenGetTasksWIthFileBackedDaoThenHistoryContainsTasksAndTasksIsViewed() {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Task> tasks = taskService.getTasks();
        for (Task task : tasks) {
            assertThat(task.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(task1, task2, task3)
                .doesNotContain(epic1, epic2, subtask1, subtask2);
    }

    @Test
    void whenDeleteAnyTaskWithDbDaoThenServiceDoesntContainsTask() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        taskService.deleteTask(task2.getId());
        taskService.deleteEpic(epic1.getId());
        assertThat(taskService.getTasks()).doesNotContain(task2);
        assertThat(taskService.getEpics()).doesNotContain(epic1);
        assertThat(taskService.getSubtasks()).doesNotContain(subtask1);

        taskService.deleteSubtask(subtask2.getId());
        assertThat(taskService.getSubtasks()).isEmpty();
    }

    @Test
    void whenDeleteAnyTaskWithInMemDaoThenServiceDoesntContainsTask() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        taskService.deleteTask(task2.getId());
        taskService.deleteEpic(epic1.getId());
        assertThat(taskService.getTasks()).doesNotContain(task2);
        assertThat(taskService.getEpics()).doesNotContain(epic1);
        assertThat(taskService.getSubtasks()).doesNotContain(subtask1);

        taskService.deleteSubtask(subtask2.getId());
        assertThat(taskService.getSubtasks()).isEmpty();
    }

    @Test
    void whenDeleteAnyTaskWithFileBackedDaoThenServiceDoesntContainsTask() {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        taskService.deleteTask(task2.getId());
        taskService.deleteEpic(epic1.getId());
        assertThat(taskService.getTasks()).doesNotContain(task2);
        assertThat(taskService.getEpics()).doesNotContain(epic1);
        assertThat(taskService.getSubtasks()).doesNotContain(subtask1);

        taskService.deleteSubtask(subtask2.getId());
        assertThat(taskService.getSubtasks()).isEmpty();
    }

    @Test
    void whenDeleteAnyTaskWithIllegalIdThenTaskNotFoundExceptionThrown() {
        int id = 666;
        assertThatThrownBy(() -> taskService.deleteTask(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task with id: " + id + " not found");

        assertThatThrownBy(() -> taskService.deleteEpic(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Epic with id: " + id + " not found");

        assertThatThrownBy(() -> taskService.deleteSubtask(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subtask with id: " + id + " not found");
    }

    @Test
    void whenUpdateTaskWithDbDaoThenTaskUpdated() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        task3.setId(task2.getId());
        Task updated = taskService.updateTask(task3).get();
        assertThat(taskService.getTask(task2.getId()).get().getName()).isEqualTo(updated.getName());
    }

    @Test
    void whenUpdateTaskWithInMemDaoThenTaskUpdated() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        task3.setId(task2.getId());
        Task updated = taskService.updateTask(task3).get();
        assertThat(taskService.getTask(task2.getId()).get().getName()).isEqualTo(updated.getName());
    }

    @Test
    void whenUpdateTaskWithFileBackedDaoThenTaskUpdated() {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        task3.setId(task2.getId());
        Task updated = taskService.updateTask(task3).get();
        assertThat(taskService.getTask(task2.getId()).get().getName()).isEqualTo(updated.getName());
    }

    @Test
    void whenUpdateAnyTaskWithNullThenIllegalExceptionThrown() {
        assertThatThrownBy(() -> taskService.updateTask(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not null required");

        assertThatThrownBy(() -> taskService.updateEpic(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic not null required");

        assertThatThrownBy(() -> taskService.updateSubtask(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subtask not null required");
    }

    @Test
    void whenUpdateAnyTaskWithIllegalIdThenTaskNotFoundThrown() {
        assertThatThrownBy(() -> taskService.updateTask(task1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task with id: " + task1.getId() + " not found");

        assertThatThrownBy(() -> taskService.updateEpic(epic1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Epic with id: " + epic1.getId() + " not found");

        assertThatThrownBy(() -> taskService.updateSubtask(subtask1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subtask with id: " + subtask1.getId() + " not found");
    }

    @Test
    void whenUpdateWithDbDaoNonViewedTaskThenHistoryDoesntContainsTask() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        task3.setId(task2.getId());
        taskService.getTask(task2.getId());
        taskService.updateTask(task3);
        assertThat(taskService.getHistory()).isEmpty();
    }

    @Test
    void whenUpdateWithInMemDaoNonViewedTaskThenHistoryDoesntContainsTask() {
        historyDao = Managers.getDefaultHistoryManager();
        taskDao = Managers.getDefaultTaskManager();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        task3.setId(task2.getId());
        taskService.getTask(task2.getId());
        taskService.updateTask(task3);
        assertThat(taskService.getHistory()).isEmpty();
    }

    @Test
    void whenUpdateWithFileBackedDaoNonViewedTaskThenHistoryDoesntContainsTask() {
        historyDao = Managers.getDefaultFileBackedHistoryManager(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        task3.setId(task2.getId());
        taskService.getTask(task2.getId());
        taskService.updateTask(task3);
        assertThat(taskService.getHistory()).isEmpty();
    }

    @Test
    void whenDeleteAllTasksWithDbDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllTasks();
        assertThat(taskService.getTasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(task1, task2);

        assertThat(taskService.getEpics()).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).containsExactly(subtask1, subtask2);
    }

    @Test
    void whenDeleteAllTasksWithInMemDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        historyDao = Managers.getDefaultHistoryManager();
        taskDao = Managers.getDefaultTaskManager();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllTasks();
        assertThat(taskService.getTasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(task1, task2);

        assertThat(taskService.getEpics()).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).containsExactly(subtask1, subtask2);
    }

    @Test
    void whenDeleteAllTasksWithFileBackedDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        historyDao = Managers.getDefaultFileBackedHistoryManager(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllTasks();
        assertThat(taskService.getTasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(task1, task2);

        assertThat(taskService.getEpics()).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).containsExactly(subtask1, subtask2);
    }

    @Test
    void whenGetEpicsWIthDbDaoThenHistoryContainsEpicsAndIsViewed() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Epic> epics = taskService.getEpics();
        for (Epic epic : epics) {
            assertThat(epic.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(epic1, epic2)
                .doesNotContain(task1, task2, subtask1, subtask2);
    }

    @Test
    void whenGetEpicsWIthInMemDaoThenHistoryContainsEpicsAndIsViewed() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Epic> epics = taskService.getEpics();
        for (Epic epic : epics) {
            assertThat(epic.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(epic1, epic2)
                .doesNotContain(task1, task2, subtask1, subtask2);
    }

    @Test
    void whenGetEpicsWIthFileBackedDaoThenHistoryContainsEpicsAndIsViewed() {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Epic> epics = taskService.getEpics();
        for (Epic epic : epics) {
            assertThat(epic.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(epic1, epic2)
                .doesNotContain(task1, task2, subtask1, subtask2);
    }

    @Test
    void whenDeleteAllEpicsWithDbDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllEpics();
        assertThat(taskService.getEpics()).isEmpty();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(epic1, epic2, subtask1, subtask2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    void whenDeleteAllEpicsWithInMemDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        historyDao = Managers.getDefaultHistoryManager();
        taskDao = Managers.getDefaultTaskManager();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllEpics();
        assertThat(taskService.getEpics()).isEmpty();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(epic1, epic2, subtask1, subtask2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    void whenDeleteAllEpicsWithFileBackedDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        historyDao = Managers.getDefaultFileBackedHistoryManager(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllEpics();
        assertThat(taskService.getEpics()).isEmpty();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(epic1, epic2, subtask1, subtask2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    void whenUpdateEpicWithDbDaoThenEpicUpdatedAndStatusRenewed() {
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        Epic epic3 = new Epic("updated", "updated desc", DONE);
        epic3.setId(epic2.getId());
        Epic updated = taskService.updateEpic(epic3).get();
        assertThat(updated.getStatus()).isEqualTo(NEW);
        assertThat(taskService.getEpic(epic2.getId()).get().getName()).isEqualTo(updated.getName());
    }

    @Test
    void whenUpdateEpicWithInMemDaoThenTaskUpdated() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        Epic epic3 = new Epic("updated", "updated desc", DONE);
        epic3.setId(epic2.getId());
        Epic updated = taskService.updateEpic(epic3).get();
        assertThat(updated.getStatus()).isEqualTo(NEW);
        assertThat(taskService.getEpic(epic2.getId()).get().getName()).isEqualTo(updated.getName());
    }

    @Test
    void whenUpdateEpicWithFileBackedDaoThenTaskUpdated() {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        Epic epic3 = new Epic("updated", "updated desc", DONE);
        epic3.setId(epic2.getId());
        Epic updated = taskService.updateEpic(epic3).get();
        assertThat(updated.getStatus()).isEqualTo(NEW);
        assertThat(taskService.getEpic(epic2.getId()).get().getName()).isEqualTo(updated.getName());
    }

    @Test
    void whenAddSubtaskWithDbDaoThenEpicStatusRenewed() {
        Epic newEpic = new Epic("new Epic", "new desc", NEW);
        Subtask newSub = new Subtask("new subtask", "any desc", IN_PROGRESS, newEpic);
        taskService.addEpic(newEpic);
        taskService.addSubtask(newSub);
        assertThat(taskService.getEpic(newEpic.getId()).get().getStatus()).isEqualTo(IN_PROGRESS);
    }

    @Test
    void whenAddSubtaskWithInMemDaoThenEpicStatusRenewed() {
        historyDao = Managers.getDefaultHistoryManager();
        taskDao = Managers.getDefaultTaskManager();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        Epic newEpic = new Epic("new Epic", "new desc", NEW);
        Subtask newSub = new Subtask("new subtask", "any desc", IN_PROGRESS, newEpic);
        taskService.addEpic(newEpic);
        taskService.addSubtask(newSub);
        assertThat(taskService.getEpic(newEpic.getId()).get().getStatus()).isEqualTo(IN_PROGRESS);
    }

    @Test
    void whenAddSubtaskWithFileBackedDaoThenEpicStatusRenewed() {
        historyDao = Managers.getDefaultFileBackedHistoryManager(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        Epic newEpic = new Epic("new Epic", "new desc", NEW);
        Subtask newSub = new Subtask("new subtask", "any desc", IN_PROGRESS, newEpic);
        taskService.addEpic(newEpic);
        taskService.addSubtask(newSub);
        assertThat(taskService.getEpic(newEpic.getId()).get().getStatus()).isEqualTo(IN_PROGRESS);
    }

    @Test
    void whenGetSubtasksWIthDbDaoThenHistoryContainsSubtasksAndSubtaskIsViewed() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Subtask> test = taskService.getSubtasks();

        for (Subtask subtask : test) {
            assertThat(subtask.isViewed()).isTrue();
        }

        assertThat(taskService.getHistory())
                .containsExactly(subtask1, subtask2)
                .doesNotContain(task1, task2, epic1, epic2);
    }

    @Test
    void whenGetSubtasksWIthInMemDaoThenHistoryContainsSubtasksAndSubtaskIsViewed() {
        historyDao = new InMemoryHistoryDao();
        taskDao = new InMemoryTaskDao();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Subtask> test = taskService.getSubtasks();
        for (Subtask subtask : test) {
            assertThat(subtask.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(subtask1, subtask2)
                .doesNotContain(task1, task2, epic1, epic2);
    }

    @Test
    void whenGetSubtasksWIthFileBackedDaoThenHistoryContainsSubtasksAndSubtaskIsViewed() {
        historyDao = new FileBackedHistoryDao(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        List<Subtask> test = taskService.getSubtasks();
        for (Subtask subtask : test) {
            assertThat(subtask.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(subtask1, subtask2)
                .doesNotContain(task1, task2, epic1, epic2);
    }

    @Test
    void whenDeleteAllSubtasksWithDbDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllSubtasks();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(subtask1, subtask2);
        assertThat(taskService.getHistory()).containsExactly(task1, task2, task3, epic1, epic2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    void whenDeleteAllSubtasksWithInMemDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        historyDao = Managers.getDefaultHistoryManager();
        taskDao = Managers.getDefaultTaskManager();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllSubtasks();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(subtask1, subtask2);
        assertThat(taskService.getHistory()).containsExactly(task1, task2, task3, epic1, epic2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    void whenDeleteAllSubtasksWithFileBackedDaoThenTaskServiceAndHistoryDoesntContainsTasks() {
        historyDao = Managers.getDefaultFileBackedHistoryManager(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllSubtasks();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(subtask1, subtask2);
        assertThat(taskService.getHistory()).containsExactly(task1, task2, task3, epic1, epic2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    void whenUpdateSubtaskButServiceDoesntContainsEpicThenTaskNotFoundThrown() {
        Subtask updated = new Subtask("upd", "desc", IN_PROGRESS,
                new Epic("epic", "desc", NEW));
        taskService.addEpic(epic1);
        taskService.addSubtask(subtask1);
        updated.setId(subtask1.getId());
        assertThatThrownBy(() -> taskService.updateSubtask(updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Epic with id: " + updated.getEpic().getId() + " not found");
    }

    @Test
    void whenUpdateWithDbDaoNonViewedSubtaskThenHistoryDoesntContainsTask() {
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);

        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        Epic epic3 = new Epic("new epic", "desc", NEW);
        taskService.addEpic(epic3);
        Subtask updated = new Subtask("upd", "desc", NEW, epic3);
        updated.setId(subtask2.getId());
        taskService.getSubtask(subtask2.getId());
        taskService.updateSubtask(updated);
        assertThat(taskService.getHistory()).isEmpty();
    }

    @Test
    void whenUpdateWithInMemDaoNonViewedSubtaskThenHistoryDoesntContainsTask() {
        historyDao = Managers.getDefaultHistoryManager();
        taskDao = Managers.getDefaultTaskManager();
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);

        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        Epic epic3 = new Epic("new epic", "desc", NEW);
        taskService.addEpic(epic3);
        Subtask updated = new Subtask("upd", "desc", NEW, epic3);
        updated.setId(subtask2.getId());
        taskService.getSubtask(subtask2.getId());
        taskService.updateSubtask(updated);
        assertThat(taskService.getHistory()).isEmpty();
    }

    @Test
    void whenUpdateWithFileBackedDaoNonViewedSubtaskThenHistoryDoesntContainsTask() {
        historyDao = Managers.getDefaultFileBackedHistoryManager(tempFile.toString());
        taskDao = Managers.getDefaultFileBackedManager(tempFile.toString());
        historyService = new HistoryServiceImpl(historyDao);
        taskService = new TaskServiceImpl(taskDao, historyService);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);

        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        Epic epic3 = new Epic("new epic", "desc", NEW);
        taskService.addEpic(epic3);
        Subtask updated = new Subtask("upd", "desc", NEW, epic3);
        updated.setId(subtask2.getId());
        taskService.getSubtask(subtask2.getId());
        taskService.updateSubtask(updated);
        assertThat(taskService.getHistory()).isEmpty();
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