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
    void whenAddMoreThanLimitThenOldestRemoved() {
        Task task3 = new Task("Name3", "Test3", Status.NEW);
        Task task4 = new Task("Name4", "Test4", Status.NEW);
        Task task5 = new Task("Name5", "Test5", Status.NEW);
        Task task6 = new Task("Name6", "Test6", Status.NEW);
        Task task7 = new Task("Name7", "Test7", Status.NEW);
        Task task8 = new Task("Name8", "Test8", Status.NEW);
        Task task9 = new Task("Name9", "Test9", Status.NEW);
        Task task10 = new Task("Name10", "Test10", Status.NEW);
        Task task11 = new Task("Name11", "Test11", Status.NEW);
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addTask(task3);
        taskManager.addTask(task4);
        taskManager.addTask(task5);
        taskManager.addTask(task6);
        taskManager.addTask(task7);
        taskManager.addTask(task8);
        taskManager.addTask(task9);
        taskManager.addTask(task10);
        taskManager.addTask(task11);
        for (int id = 1; id <= taskManager.getTasks().size(); id++) {
            taskManager.getTask(id);
        }
        List<Task> expected = List.of(
                secondTask, task3, task4, task5, task6, task7, task8, task9, task10, task11
        );
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenGetHistoryThenExpectedResult() {
        historyManager.addToHistory(firstTask);
        historyManager.addToHistory(secondTask);
        List<Task> expected = List.of(firstTask, secondTask);
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).hasSameElementsAs(expected);
    }
}