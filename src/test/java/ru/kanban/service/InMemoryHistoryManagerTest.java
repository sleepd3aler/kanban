package ru.kanban.service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private InMemoryTaskManager taskManager;
    private Task firstTask;
    private Task secondTask;
    private Epic firstEpic;
    private Epic secondEpic;
    private Subtask firstSubtask;
    private Subtask secondSubtask;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
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
    void whenRemoveFromTaskManagerThenHistoryDoesntContainsTask() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTask(1);
        taskManager.getTask(2);
        taskManager.deleteTask(1);
        taskManager.deleteTask(2);
        List<Task> res = taskManager.getHistory();
        assertThat(res).doesNotContain(firstTask, secondTask);
    }

    @Test
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
        List<Task> res = taskManager.getHistory();
        assertThat(res).doesNotContain(secondTask, secondEpic, firstSubtask);
    }

    @Test
    void whenGetSameTaskThenHistoryDoesntContainsDuplicates() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(firstTask.getId());
        List<Task> expected = taskManager.getHistory();
        assertThat(expected).hasSize(1)
                .doesNotHaveDuplicates();
    }

    @Test
    void whenGetAllTasksThenHistoryHasAllTasks() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.getTasks();
        assertThat(historyManager.getViewedTasks())
                .hasSize(2)
                .containsExactly(firstTask, secondTask);
    }

    @Test
    void whenGetAllEpicsThenHistoryHasAllTasks() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.getEpics();
        assertThat(historyManager.getViewedTasks())
                .hasSize(2)
                .containsExactly(firstEpic, secondEpic);
    }

    @Test
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