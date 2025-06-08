package ru.kanban;

import java.util.List;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
import ru.kanban.service.InMemoryTaskManager;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task("Task 1", "Description 1", Status.IN_PROGRESS);
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        Task task3 = new Task("Task 3", "Description 3", Status.IN_PROGRESS);
        Task task4 = new Task("Task 4", "Description 4", Status.IN_PROGRESS);
        Task task5 = new Task("Task 5", "Description 5", Status.IN_PROGRESS);
        Epic epic1 = new Epic("Epic 1", "Description 1", Status.IN_PROGRESS);
        Epic epic2 = new Epic("Epic 2", "Description 2", Status.IN_PROGRESS);
        Epic epic3 = new Epic("Epic 3", "Description 3", Status.IN_PROGRESS);
        Subtask subtask1 = new Subtask("Subtask1", "Description1", Status.NEW, epic1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", Status.NEW, epic2);
        Subtask subtask3 = new Subtask("Subtask3", "Description3", Status.NEW, epic3);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        taskManager.addTask(task4);
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addEpic(epic3);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());
        taskManager.getTask(task4.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getEpic(epic3.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getSubtask(subtask3.getId());
        taskManager.addTask(task5);
        taskManager.getTask(task5.getId());
        List<Task> test = taskManager.getHistory();
        test.forEach(System.out::println);
    }
}