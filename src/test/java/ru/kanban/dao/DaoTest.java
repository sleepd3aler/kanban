package ru.kanban.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.kanban.model.Status.IN_PROGRESS;
import static ru.kanban.model.Status.NEW;

public abstract class DaoTest {
    protected TaskDao taskDao;

    protected Task task1;
    protected Task task2;
    protected Task task3;
    protected Epic epic1;
    protected Epic epic2;
    protected Subtask subtask1;
    protected Subtask subtask2;

    abstract TaskDao createDao() throws IOException;

    @BeforeEach
    void setUp() throws IOException {
        taskDao = createDao();
        task1 = new Task("task1", "desc", NEW);
        task2 = new Task("task2", "desc", IN_PROGRESS);
        task3 = new Task("task3", "desc", IN_PROGRESS);
        epic1 = new Epic("epic1", "desc", NEW);
        epic2 = new Epic("epic2", "desc", NEW);
        subtask1 = new Subtask("subtask1", "desc", NEW, epic1);
        subtask2 = new Subtask("subtask2", "desc", NEW, epic2);
    }

    @Test
    void whenGetTaskWithIllegalIdThenResIsEmpty() {
        assertThat(taskDao.getTask(55)).isEmpty();
    }

    @Test
    void whenAddTaskThenGetByIdIsSame() {
        taskDao.addTask(task1);
        Task expected = new Task(task1.getName(), task1.getDescription(), task1.getStatus());
        expected.setId(task1.getId());
        Task result = taskDao.getTask(task1.getId()).get();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenGetTasksThenExpectedResult() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        taskDao.addSubtask(subtask1);
        List<Task> expected = List.of(task1, task2);
        assertThat(taskDao.getTasks()).isEqualTo(expected);
    }

    @Test
    void whenGetTasksAndDbHasNoTasksThenEmptyRes() {
        taskDao.addEpic(epic1);
        taskDao.addSubtask(subtask1);
        assertThat(taskDao.getTasks()).isEmpty();
    }

    @Test
    void whenDeleteTaskThenDaoDoesntContainsById() throws SQLException {
        taskDao.addTask(task1);
        taskDao.addTask(task2);

        Task deleted = taskDao.deleteTask(task2.getId()).get();
        assertThat(taskDao.getTask(task2.getId())).isEmpty();
    }

    @Test
    void whenTryToDeleteWithIncorrectTypeThenTableIsSame() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        assertThat(taskDao.deleteTask(epic1.getId())).isEmpty();
    }

    @Test
    void whenUpdateTaskSuccessful() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        task3.setId(task2.getId());
        taskDao.updateTask(task3);
        assertThat(taskDao.getTask(task3.getId()).get().getName()).isEqualTo(task3.getName());
        assertThat(taskDao.getTask(task3.getId()).get().getDescription()).isEqualTo(task3.getDescription());
        assertThat(taskDao.getTask(task3.getId()).get().getStatus()).isEqualTo(task3.getStatus());
        assertThat(taskDao.getTasks()).hasSize(2);
    }

    @Test
    void whenUpdateTaskFailed() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        task3.setId(3);
        taskDao.updateTask(task3);
        List<Task> expected = List.of(task1, task2);
        assertThat(taskDao.getTasks()).isEqualTo(expected);
    }

    @Test
    void whenDeleteAllTasksThenTableDoesntExistsByIdAnyTasks() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addTask(task3);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.deleteAllTasks();
        assertThat(taskDao.getTasks()).isEmpty();
    }

    @Test
    void whenDeleteAllEpicsThenDaoDoesntContainEpics() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.deleteAllEpics();
        assertThat(taskDao.getEpics()).isEmpty();
    }

    @Test
    void whenAddEpicThenGetByIdIsSame() {
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        assertThat(taskDao.getEpic(epic1.getId()).get()).isEqualTo(epic1);
    }

    @Test
    void whenGetEpicsThenOnlyEpicsExpected() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        List<Epic> expected = List.of(epic1, epic2);
        assertThat(taskDao.getEpics()).isEqualTo(expected)
                .containsOnly(epic1, epic2);
    }

    @Test
    void whenDaoHasNoEpicsGetEpicsIsEmpty() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        assertThat(taskDao.getEpics()).isEmpty();
    }

    @Test
    void whenDeleteEpicThenDaoDoesntExistsByIdIt() {
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.deleteEpic(epic1.getId());
        assertThat(taskDao.getEpics()).doesNotContain(epic1);
    }

    @Test
    void whenTryDeleteEpicWithWrongId() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.deleteEpic(task2.getId());
        assertThat(taskDao.getTasks()).contains(task1, task2);
    }

    @Test
    void whenUpdateEpicSuccessful() {
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        Epic epic = new Epic("epic3", "desc", NEW);
        epic.setId(epic2.getId());
        taskDao.updateEpic(epic);
        assertThat(taskDao.getEpic(epic2.getId()).get()).isEqualTo(epic);
        assertThat(taskDao.getEpics()).hasSize(2);
    }

    @Test
    void whenAddSubtaskThenGetByIdIsSame() {
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.addSubtask(subtask1);
        taskDao.addSubtask(subtask2);
        assertThat(taskDao.getSubtask(subtask1.getId()).get()).isNotNull();
    }

    @Test
    void whenGetSubtasksThenExpectedSubtasksOnly() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.addSubtask(subtask1);
        taskDao.addSubtask(subtask2);
        assertThat(taskDao.getSubtasks()).containsOnly(subtask1, subtask2);
    }

    @Test
    void whenTableDoesntContainSubtasksGetSubtasksIsEmpty() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        assertThat(taskDao.getSubtasks()).isEmpty();
    }

    @Test
    void whenDeleteSubtaskSuccessful() {
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.addSubtask(subtask1);
        taskDao.addSubtask(subtask2);
        taskDao.deleteSubtask(subtask1.getId());
        assertThat(taskDao.getSubtasks()).hasSize(1).containsOnly(subtask2);
    }

    @Test
    void whenDeleteSubtaskByIncorrectIdSubtaskListIsSame() {
        taskDao.addTask(task1);
        taskDao.addEpic(epic1);
        taskDao.addSubtask(subtask1);
        taskDao.deleteSubtask(task1.getId());
        assertThat(taskDao.getTasks()).containsOnly(task1);
    }

    @Test
    void whenDeleteAllSubtasksTableWontContainAnySubtasks() {
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.addSubtask(subtask1);
        taskDao.addSubtask(subtask2);
        taskDao.deleteAllSubtasks();
        assertThat(taskDao.getSubtasks()).isEmpty();
        assertThat(taskDao.getEpics()).hasSize(2);
        assertThat(taskDao.getTasks()).hasSize(2);
    }

    @Test
    void whenUpdateSubtaskSuccessful() {
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.addSubtask(subtask1);
        taskDao.addSubtask(subtask2);
        Subtask subtask = new Subtask("sub3", "desc", NEW, epic1);
        subtask.setId(subtask1.getId());
        taskDao.updateSubtask(subtask);
        assertThat(taskDao.getSubtask(subtask1.getId()).get()).isEqualTo(subtask);
    }
}
