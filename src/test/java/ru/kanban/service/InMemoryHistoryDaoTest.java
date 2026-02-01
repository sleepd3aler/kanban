package ru.kanban.service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.kanban.dao.InMemoryHistoryDao;
import ru.kanban.dao.InMemoryTaskDao;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryHistoryDaoTest {

    private InMemoryHistoryDao historyManager;
    private InMemoryTaskDao taskManager;
    private Task firstTask;
    private Task secondTask;
    private Epic firstEpic;
    private Epic secondEpic;
    private Subtask firstSubtask;
    private Subtask secondSubtask;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryDao();
        taskManager = new InMemoryTaskDao();
        firstTask = new Task("First Task", "First Task", Status.NEW);
        secondTask = new Task("Second Task", "Second Task", Status.NEW);
        firstEpic = new Epic("First", "First Epic", Status.NEW);
        secondEpic = new Epic("Second", "Second Epic", Status.NEW);
        firstSubtask = new Subtask("First subtask", "Subtask of First Epic", Status.NEW, firstEpic);
        secondSubtask = new Subtask("Second Subtask", "Subtask of Second Epic", Status.NEW, secondEpic);

    }

    @Test
    void whenSetToViewedThenSuccess() {
        historyManager.setToViewed(firstTask);
        boolean expected = true;
        assertThat(firstTask.isViewed()).isEqualTo(expected);
    }

    @Test
    @Disabled
    void whenGetTaskThenAddToHistory() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(secondTask.getId());
        List<Task> expected = List.of(firstTask, secondTask);
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @Disabled
    void whenGetEpicThenAddToHistory() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.getEpic(firstEpic.getId());
        taskManager.getEpic(secondEpic.getId());
        List<Task> expected = List.of(firstEpic, secondEpic);
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @Disabled
    void whenGetSubtaskThenAddToHistory() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getSubtask(firstSubtask.getId());
        taskManager.getSubtask(secondSubtask.getId());
        List<Task> expected = List.of(firstSubtask, secondSubtask);
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenGetHistoryThenExpectedResult() {
        historyManager.addToHistory(firstTask);
        secondTask.setId(2);
        historyManager.addToHistory(secondTask);
        List<Task> expected = List.of(firstTask, secondTask);
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    void whenRemoveThenHistoryManagerDoesntContainsTask() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTask(1);
        taskManager.getTask(2);
        historyManager.remove(1);
        List<Task> res = historyManager.getViewedTasks();
        assertThat(res).doesNotContain(firstTask);
    }

    @Test
    @Disabled
    void whenRemoveFromTaskManagerThenHistoryDoesntContainsTask() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTask(1);
        taskManager.getTask(2);
        taskManager.deleteTask(1);
        taskManager.deleteTask(2);

    }

    @Test
    @Disabled
    void whenRemoveAnyKindOfTaskThenHistoryDoesntContains() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(secondTask.getId());
        taskManager.getEpic(firstEpic.getId());
        taskManager.getEpic(secondEpic.getId());
        taskManager.getSubtask(firstSubtask.getId());
        taskManager.getSubtask(secondSubtask.getId());
        taskManager.deleteTask(secondTask.getId());
        taskManager.deleteEpic(secondEpic.getId());
        taskManager.deleteSubtask(firstSubtask.getId());

    }

    @Test
    @Disabled
    void whenGetSameTaskThenHistoryDoesntContainsDuplicates() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(firstTask.getId());

    }

    @Test
    @Disabled
    void whenGetAllTasksThenHistoryHasAllTasks() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTasks();
        assertThat(historyManager.getViewedTasks())
                .hasSize(2)
                .containsExactly(firstTask, secondTask);
    }

    @Test
    @Disabled
    void whenGetAllEpicsThenHistoryHasAllTasks() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.getEpics();
        assertThat(historyManager.getViewedTasks())
                .hasSize(2)
                .containsExactly(firstEpic, secondEpic);
    }

    @Test
    @Disabled
    void whenGetAllSubtasksThenHistoryHasAllSubtasks() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getSubtasks();
        assertThat(historyManager.getViewedTasks())
                .hasSize(2)
                .containsExactly(firstSubtask, secondSubtask);
    }

    @Test
    @Disabled
    void whenGetAllTasksEpicsAndSubtasksThenHistoryHasAllItems() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getTasks();
        taskManager.getEpics();
        taskManager.getSubtasks();
        assertThat(historyManager.getViewedTasks())
                .hasSize(6)
                .containsExactly(firstTask, secondTask, firstEpic, secondEpic, firstSubtask, secondSubtask);
    }

    @Test
    @Disabled
    void whenDeleteTaskFromTheMiddle() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addTask(firstEpic);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(secondTask.getId());
        taskManager.getTask(firstEpic.getId());
        historyManager.remove(secondTask.getId());
        List<Task> expected = historyManager.getViewedTasks();
        assertThat(expected)
                .hasSize(2)
                .containsExactly(firstTask, firstEpic);
    }

    @Test
    @Disabled
    void whenDeleteFirstViewedTaskThenSecondTaskBecomesFirstViewed() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addTask(firstEpic);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(secondTask.getId());
        taskManager.getTask(firstEpic.getId());
        historyManager.remove(firstTask.getId());
        List<Task> expected = historyManager.getViewedTasks();
        assertThat(expected)
                .hasSize(2)
                .containsExactly(secondTask, firstEpic);
    }

    @Test
    @Disabled
    void whenDeleteLastViewedTaskThenSecondTaskBecomesFirstViewed() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addTask(firstEpic);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(secondTask.getId());
        taskManager.getTask(firstEpic.getId());
        historyManager.remove(firstEpic.getId());
        List<Task> expected = historyManager.getViewedTasks();
        assertThat(expected)
                .hasSize(2)
                .containsExactly(firstTask, secondTask);
    }

    @Test
    @Disabled
    void whenDeleteViewedTaskFromManagerHistoryDoesntContainsIt() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(secondTask.getId());
        taskManager.deleteTask(firstTask.getId());
        assertThat(historyManager.getViewedTasks())
                .hasSize(1)
                .doesNotContain(firstTask);
    }

    @Test
    @Disabled
    void whenDeleteViewedEpicFromManagerHistoryDoesntContainsIt() {
        taskManager.addTask(firstEpic);
        taskManager.addTask(secondTask);
        taskManager.getTask(firstEpic.getId());
        taskManager.getTask(secondTask.getId());
        taskManager.deleteTask(firstEpic.getId());
        assertThat(historyManager.getViewedTasks())
                .hasSize(1)
                .doesNotContain(firstEpic);
    }

    @Test
    @Disabled
    void whenDeleteNonViewedTaskThenHistoryIsSame() {
        Task thirdTask = new Task("Third", "Test", Status.NEW);
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addTask(thirdTask);
        taskManager.getTasks();
        historyManager.remove(4);
        assertThat(historyManager.getViewedTasks()).hasSize(3)
                .containsExactly(firstTask, secondTask, thirdTask);
    }

    @Test
    @Disabled
    void whenDeleteViewedSubtaskFromManagerThenHistoryDoesntContainsIt() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.getEpic(firstEpic.getId());
        taskManager.getSubtask(firstSubtask.getId());
        taskManager.deleteSubtask(firstSubtask.getId());
        assertThat(historyManager.getViewedTasks()).hasSize(1)
                .doesNotContain(firstSubtask);
    }

    @Test
    void whenDeleteAllTasksFromManagerThenHistoryIsEmpty() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTasks();
        taskManager.deleteAllTasks();
        assertThat(historyManager.getViewedTasks()).isEmpty();
    }

    @Test
    void whenDeleteAllEpicsFromManagerThenHistoryIsEmpty() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getEpics();
        taskManager.deleteAllEpics();
        assertThat(historyManager.getViewedTasks())
                .isEmpty();
    }

    @Test
    void whenDeleteAllSubtasksFromManagerThenHistoryIsEmpty() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getSubtasks();
        taskManager.deleteAllSubtasks();
        assertThat(historyManager.getViewedTasks()).isEmpty();
    }

    @Test
    @Disabled
    void whenHistoryContainsMixedTypesAndDeleteAllTasksFromManagerThenHistoryDoesntContainsTasksOnly() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getTasks();
        taskManager.getEpics();
        taskManager.getSubtasks();
        taskManager.deleteAllTasks();
        assertThat(historyManager.getViewedTasks())
                .hasSize(4)
                .doesNotContain(firstTask, secondTask)
                .containsExactly(firstEpic, secondEpic, firstSubtask, secondSubtask);
    }

    @Test
    @Disabled
    void whenHistoryContainsMixedTypesAndDeleteAllEpicsFromManagerThenHistoryDoesntContainsEpicsAndSubtasks() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getTasks();
        taskManager.getEpics();
        taskManager.getSubtasks();
        taskManager.deleteAllEpics();
        assertThat(historyManager.getViewedTasks())
                .hasSize(2)
                .doesNotContain(firstEpic, secondEpic, firstSubtask, secondSubtask)
                .containsExactly(firstTask, secondTask);
    }

    @Test
    @Disabled
    void whenHistoryContainsMixedTypesAndDeleteAllSubtasksFromManagerThenHistoryDoesntContainsSubtasks() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.getTasks();
        taskManager.getEpics();
        taskManager.getSubtasks();
        taskManager.deleteAllSubtasks();
        assertThat(historyManager.getViewedTasks())
                .hasSize(4)
                .doesNotContain(firstSubtask, secondSubtask)
                .containsExactly(firstTask, secondTask, firstEpic, secondEpic);
    }
}