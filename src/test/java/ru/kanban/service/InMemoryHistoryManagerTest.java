package ru.kanban.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryHistoryManagerTest {

    @Test
    void whenSetToViewedThenSuccess() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task test = new Task("Name", "Test", Status.NEW);
        boolean expected = true;
        historyManager.setToViewed(test);
        assertThat(test.isViewed()).isEqualTo(expected);
    }

    @Test
    void whenGetTaskThenAddToHistory() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        InMemoryTaskManager manager = new InMemoryTaskManager(historyManager);
        manager.addTask(new Task("Name", "Test", Status.NEW));
        manager.addTask(new Task("Name2", "Test2", Status.NEW));
        manager.getTask(1);
        manager.getTask(2);
        List<Task> expected = List.of(
                new Task("Name", "Test", Status.NEW),
                new Task("Name2", "Test2", Status.NEW));
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenGetEpicThenAddToHistory() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        InMemoryTaskManager manager = new InMemoryTaskManager(historyManager);
        manager.addEpic(new Epic("Name", "Test", Status.NEW));
        manager.addEpic(new Epic("Name2", "Test2", Status.NEW));
        manager.getEpic(1);
        manager.getEpic(2);
        List<Task> expected = List.of(
                new Epic("Name", "Test", Status.NEW),
                new Epic("Name2", "Test2", Status.NEW));
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenGetSubtaskThenAddToHistory() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        InMemoryTaskManager manager = new InMemoryTaskManager(historyManager);
        Epic epic1 = new Epic("Name", "Test", Status.NEW);
        Epic epic2 = new Epic("Name2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        Subtask subtask1 = new Subtask("Name1", "Subtask of test", Status.NEW, epic1);
        Subtask subtask2 = new Subtask("Name2", "Subtask of test2", Status.NEW, epic2);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.getSubtask(3);
        manager.getSubtask(4);
        List<Task> expected = List.of(subtask1, subtask2);
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenAddMoreThanLimitThenOldestRemoved() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        InMemoryTaskManager manager = new InMemoryTaskManager(historyManager);
        Task task1 = new Task("Name", "Test", Status.NEW);
        Task task2 = new Task("Name2", "Test2", Status.NEW);
        Task task3 = new Task("Name3", "Test3", Status.NEW);
        Task task4 = new Task("Name4", "Test4", Status.NEW);
        Task task5 = new Task("Name5", "Test5", Status.NEW);
        Task task6 = new Task("Name6", "Test6", Status.NEW);
        Task task7 = new Task("Name7", "Test7", Status.NEW);
        Task task8 = new Task("Name8", "Test8", Status.NEW);
        Task task9 = new Task("Name9", "Test9", Status.NEW);
        Task task10 = new Task("Name10", "Test10", Status.NEW);
        Task task11 = new Task("Name11", "Test11", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addTask(task4);
        manager.addTask(task5);
        manager.addTask(task6);
        manager.addTask(task7);
        manager.addTask(task8);
        manager.addTask(task9);
        manager.addTask(task10);
        manager.addTask(task11);
        for (int id = 1; id <= manager.getTasks().size(); id++) {
            manager.getTask(id);
        }
        List<Task> expected = List.of(
                task2, task3, task4, task5, task6, task7, task8, task9, task10, task11
        );
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenGetHistoryThenSuccess() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task test = new Task("Name", "Test", Status.NEW);
        Task test2 = new Task("Name2", "Test2", Status.NEW);
        historyManager.addToHistory(test);
        historyManager.addToHistory(test2);
        List<Task> expected = List.of(test, test2);
        List<Task> result = historyManager.getViewedTasks();
        assertThat(result).hasSameElementsAs(expected);
    }
}