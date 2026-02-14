package ru.kanban.service;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.*;
import ru.kanban.dao.DbHistoryDao;
import ru.kanban.dao.DbTaskDao;
import ru.kanban.dao.HistoryDao;
import ru.kanban.dao.TaskDao;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.kanban.model.Status.*;

class DbTaskDaoTest {
    private static Connection connection;

    private TaskDao manager;
    private HistoryDao historyManager;

    private Task task1;
    private Task task2;
    private Task task3;

    private Epic epic1;
    private Epic epic2;

    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = DbTaskDao.class.getClassLoader().getResourceAsStream("db/test.properties")) {
            Properties config = new Properties();
            config.load(in);
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE from history; DELETE from tasks"
        )) {
            statement.execute();
        }
    }

    @BeforeEach
    void setUp() {
        historyManager = new DbHistoryDao(connection);
        manager = new DbTaskDao(connection);
        task1 = new Task("task1", "desc", NEW);
        task2 = new Task("task2", "desc", IN_PROGRESS);
        task3 = new Task("task3", "desc", NEW);
        epic1 = new Epic("epic1", "desc", NEW);
        epic2 = new Epic("epic2", "desc", NEW);
        subtask1 = new Subtask("subtask1", "desc", NEW, epic1);
        subtask2 = new Subtask("subtask2", "desc", NEW, epic2);
    }

    @Test
    void whenGetTaskWithIllegalIdThenResIsEmpty() {
        assertThat(manager.getTask(55)).isEmpty();
    }

    @Test
    void whenAddTaskThenGetByIdIsSame() {
        manager.addTask(task1);
        Task expected = new Task(task1.getName(), task1.getDescription(), task1.getStatus());
        expected.setId(task1.getId());
        Task result = manager.getTask(task1.getId()).get();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenGetTasksThenExpectedResult() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        List<Task> expected = List.of(task1, task2, task3);
        assertThat(manager.getTasks()).isEqualTo(expected);
    }

    @Test
    void whenGetTasksAndDbHasNoTasksThenEmptyRes() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        assertThat(manager.getTasks()).isEmpty();
    }

    @Test
    void whenDeleteTaskThenDbDoesntExistsById() throws SQLException {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        Task deleted = manager.deleteTask(task2.getId()).get();
        List<Task> afterDeleteList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from tasks"
        )) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Task task = new Task(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status"))
                );
                task.setId(resultSet.getInt(1));
                afterDeleteList.add(task);
            }
        }
        assertThat(afterDeleteList).doesNotContain(deleted);
    }

    @Test
    void whenTryToDeleteWithIncorrectTypeThenTableIsSame() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        assertThat(manager.deleteTask(epic1.getId())).isEmpty();
    }

    @Test
    void whenUpdateTaskSuccessful() {
        manager.addTask(task1);
        manager.addTask(task2);
        task3.setId(task2.getId());
        manager.updateTask(task3);
        assertThat(manager.getTask(task3.getId()).get().getName()).isEqualTo(task3.getName());
        assertThat(manager.getTask(task3.getId()).get().getDescription()).isEqualTo(task3.getDescription());
        assertThat(manager.getTask(task3.getId()).get().getStatus()).isEqualTo(task3.getStatus());
        assertThat(manager.getTasks()).hasSize(2);
    }

    @Test
    void whenUpdateTaskFailed() {
        manager.addTask(task1);
        manager.addTask(task2);
        task3.setId(3);
        manager.updateTask(task3);
        List<Task> expected = List.of(task1, task2);
        assertThat(manager.getTasks()).isEqualTo(expected);
    }

    @Test
    void whenDeleteAllTasksThenTableDoesntExistsByIdAnyTasks() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.deleteAllTasks();
        assertThat(manager.getTasks()).isEmpty();
    }

    @Test
    void whenAddEpicThenGetByIdIsSame() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        assertThat(manager.getEpic(epic1.getId()).get()).isEqualTo(epic1);
    }

    @Test
    void whenGetEpicsThenOnlyEpicsExpected() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        List<Epic> expected = List.of(epic1, epic2);
        assertThat(manager.getEpics()).isEqualTo(expected)
                .containsOnly(epic1, epic2);
    }

    @Test
    void whenManagerHasNoEpicsGetEpicsIsEmpty() {
        manager.addTask(task1);
        manager.addTask(task2);
        assertThat(manager.getEpics()).isEmpty();
    }

    @Test
    void whenDeleteEpicThenManagerDoesntExistsByIdIt() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.deleteEpic(epic1.getId());
        assertThat(manager.getEpics()).doesNotContain(epic1);
    }

    @Test
    void whenTryDeleteEpicWithWrongId() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.deleteEpic(task2.getId());
        assertThat(manager.getTasks()).contains(task1, task2);
    }

    @Test
    void whenDeleteAllEpicsThenManagerDoesntExistsByIdEpics() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.deleteAllEpics();
        assertThat(manager.getEpics()).isEmpty();
    }

    @Test
    void whenUpdateEpicSuccessful() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        Epic epic = new Epic("epic3", "desc", NEW);
        epic.setId(epic2.getId());
        manager.updateEpic(epic);
        assertThat(manager.getEpic(epic2.getId()).get()).isEqualTo(epic);
        assertThat(manager.getEpics()).hasSize(2);
    }

    @Test
    @Disabled
    void whenAddSubtaskThenGetByIdIsSameAndViewed() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        assertThat(manager.getSubtask(subtask1.getId()).get()).isNotNull();
        assertThat(manager.getSubtask(subtask1.getId()).get().isViewed()).isTrue();
    }

    @Test
    @Disabled
    void whenAddDoneSubtaskThenEpicStatusDONE() {
        manager.addEpic(epic1);
        subtask1.setStatus(DONE);
        manager.addSubtask(subtask1);
        assertThat(manager.getEpic(epic1.getId()).get().getStatus()).isEqualTo(DONE);
    }

    @Test
    @Disabled
    void whenAddNewSubtaskThenEpicStatusIN_PROGRESS() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        assertThat(manager.getEpic(epic1.getId()).get().getStatus()).isEqualTo(NEW);
        manager.deleteSubtask(subtask1.getId());
        subtask1.setStatus(IN_PROGRESS);
        manager.addSubtask(subtask1);
        assertThat(manager.getEpic(epic1.getId()).get().getStatus()).isEqualTo(IN_PROGRESS);
    }

    @Test
    void whenGetSubtasksThenExpectedSubtasksOnly() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        assertThat(manager.getSubtasks()).containsOnly(subtask1, subtask2);
    }

    @Test
    void whenTableDoesntContainSubtasksGetSubtasksIsEmpty() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        assertThat(manager.getSubtasks()).isEmpty();
    }

    @Test
    void whenDeleteSubtaskSuccessful() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.deleteSubtask(subtask1.getId());
        assertThat(manager.getSubtasks()).hasSize(1).containsOnly(subtask2);
    }

    @Test
    void whenDeleteSubtaskByIncorrectIdTableIsSame() {
        manager.addTask(task1);
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.deleteSubtask(task1.getId());
        assertThat(manager.getTasks()).containsOnly(task1);
    }

    @Test
    void whenDeleteSubtaskThenEpicStatusUpdated() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.deleteSubtask(subtask1.getId());
        assertThat(manager.getEpic((epic1.getId())).get().getStatus()).isEqualTo(NEW);
    }

    @Test
    void whenDeleteAllSubtasksTableWontContainAnySubtasks() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.deleteAllSubtasks();
        assertThat(manager.getSubtasks()).isEmpty();
        assertThat(manager.getEpics()).hasSize(2);
        assertThat(manager.getTasks()).hasSize(2);
    }

    @Test
    void whenDeleteAllSubtasksThenEpicsStatusNEW() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.deleteAllSubtasks();
        List<Epic> epics = manager.getEpics();
        assertThat(epics.stream()
                .allMatch(epic -> epic.getStatus().equals(NEW)))
                .isTrue();
    }

    @Test
    void whenUpdateSubtaskSuccessful() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        Subtask subtask = new Subtask("sub3", "desc", NEW, epic1);
        subtask.setId(subtask1.getId());
        manager.updateSubtask(subtask);
        assertThat(manager.getSubtask(subtask1.getId()).get()).isEqualTo(subtask);
    }

    @Test
    @Disabled
    void whenUpdateSubtaskThenEpicStatusRenewed() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        subtask1.setStatus(IN_PROGRESS);
        manager.updateSubtask(subtask1);
        assertThat(manager.getEpic(epic1.getId()).get().getStatus()).isEqualTo(IN_PROGRESS);
    }

    @Test
    @Disabled
    void whenGetTaskThenHistoryExistsByIdTask() {
        manager.addTask(task2);
        manager.getTask(task2.getId());
        assertThat(historyManager.getViewedTasks()).contains(task2);
    }

    @Test
    @Disabled
    void whenGetAllTasksThenHistoryExistsByIdOnlyLast10Viewed() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        for (int i = 4; i <= 11; i++) {
            manager.addTask(new Task("task" + i, "desc", NEW));
        }
        List<Task> result = manager.getTasks();
        assertThat(historyManager.getViewedTasks()).hasSize(10).doesNotContain(task1);
    }

    @Test
    void whenDeleteFromTasksThenHistoryDoesntExistsByIdSame() {
        manager.addTask(task1);
        manager.getTask(task1.getId());
        manager.deleteTask(task1.getId());
        assertThat(historyManager.getViewedTasks()).doesNotContain(task1);
    }

    @Test
    void whenUpdateTaskAndSetNotViewedThenHistoryDoesntExistsByIdTask() {
        manager.addTask(task1);
        manager.getTask(task1.getId());
        task2.setId(task1.getId());
        manager.updateTask(task2);
        assertThat(historyManager.getViewedTasks()).isEmpty();
    }

    @Disabled
    @Test
    void whenDeleteAllTasksHistoryDoesntContainTasks() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task3.getId());
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.deleteAllTasks();
//        assertThat(manager.getHistory()).doesNotContain(task1, task2, task3);
    }

    @Test
    @Disabled
    void whenGetEpicThenHistoryExistsByIdEpic() {
        manager.addEpic(epic1);
        manager.getEpic(epic1.getId());
        assertThat(historyManager.getViewedTasks()).contains(epic1);
    }

    @Test
    @Disabled
    void whenGetAllEpicsThenHistoryExistsByIdOnlyLast10Viewed() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        for (int i = 3; i <= 11; i++) {
            manager.addTask(new Epic("task" + i, "desc", NEW));
        }
        List<Epic> result = manager.getEpics();
        assertThat(historyManager.getViewedTasks()).hasSize(10).doesNotContain(epic1);
    }

    @Test
    void whenDeleteFromEpicsThenHistoryDoesntExistsByIdEpicsAndSubtasks() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.deleteEpic(epic1.getId());
        assertThat(historyManager.getViewedTasks()).doesNotContain(epic1);
        assertThat(historyManager.getViewedTasks()).doesNotContain(subtask1);

    }

    @Test
    void whenUpdateEpicAndSetNotViewedThenHistoryDoesntExistsByIdTask() {
        manager.addEpic(epic1);
        manager.getEpic(epic1.getId());
        epic2.setId(epic1.getId());
        manager.updateEpic(epic2);
        assertThat(historyManager.getViewedTasks()).isEmpty();
    }

    @Test
    @Disabled
    void whenDeleteAllEpicsHistoryDoesntContainEpicsAndSubtasks() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task3.getId());
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.deleteAllEpics();
//        assertThat(manager.getHistory()).doesNotContain(epic1, epic2, subtask1, subtask2);
    }

    @Test
    @Disabled
    void whenGetSubtaskThenHistoryExistsByIdSame() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.getSubtask(subtask1.getId());
        assertThat(historyManager.getViewedTasks()).contains(subtask1);
    }

    @Test
    @Disabled
    void whenGetAllSubtasksThenHistoryExistsByIdOnlyLast10Viewed() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        for (int i = 3; i <= 11; i++) {
            Epic epic = new Epic("epic" + i, "desc", NEW);
            Subtask subtask = new Subtask("subtask" + i, "desc", NEW, epic);
            manager.addEpic(epic);
            manager.addSubtask(subtask);
        }
        List<Subtask> result = manager.getSubtasks();
        assertThat(historyManager.getViewedTasks()).hasSize(10).doesNotContain(subtask1);
    }

    @Test
    void whenDeleteFromSubtasksThenHistoryDoesntExistsByIdSubtasks() {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.deleteEpic(epic1.getId());
        assertThat(historyManager.getViewedTasks()).doesNotContain(epic1);
        assertThat(historyManager.getViewedTasks()).doesNotContain(subtask1);

    }

    @Test
    void whenUpdateSubtaskAndSetNotViewedThenHistoryDoesntExistsByIdPrevious() {
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addSubtask(subtask1);
        manager.getSubtask(subtask1.getId());
        subtask2.setId(subtask1.getId());

        manager.updateSubtask(subtask2);
        assertThat(historyManager.getViewedTasks()).isEmpty();
    }

    @Test
    @Disabled
    void whenDeleteAllSubtasksHistoryDoesntContainSubtasks() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task3.getId());
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.deleteAllSubtasks();
    }
}