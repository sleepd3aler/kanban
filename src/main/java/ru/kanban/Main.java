package ru.kanban;

import java.sql.Connection;
import java.util.List;
import ru.kanban.configurations.Config;
import ru.kanban.dao.DbHistoryDao;
import ru.kanban.dao.DbTaskDao;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
import ru.kanban.service.HistoryService;
import ru.kanban.service.HistoryServiceImpl;
import ru.kanban.service.TaskService;
import ru.kanban.service.TaskServiceImpl;
import ru.kanban.utils.DbUtils;
import ru.kanban.utils.Managers;
import ru.kanban.validator.TaskValidator;

public class Main {
    public static void main(String[] args) {
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
        Config config = new Config();
        config.load("/db/liquibase.properties");

        try (Connection connection = DbUtils.getConnection(config);
             DbHistoryDao historyDao = Managers.getDbHistoryManager(connection);
             DbTaskDao taskDao = Managers.getDbManager(connection)
        ) {
            TaskValidator validator = new TaskValidator();
            HistoryService historyService = new HistoryServiceImpl(historyDao);
            TaskService taskService = new TaskServiceImpl(taskDao, historyService, validator);
            taskService.addTask(task1);
            taskService.addTask(task2);
            taskService.addTask(task3);
            taskService.addTask(task4);
            taskService.addEpic(epic1);
            taskService.addEpic(epic2);
            taskService.addEpic(epic3);
            taskService.addSubtask(subtask1);
            taskService.addSubtask(subtask2);
            taskService.addSubtask(subtask3);
            taskService.getTask(task1.getId());
            taskService.getTask(task2.getId());
            taskService.getTask(task3.getId());
            taskService.getTask(task4.getId());
            taskService.getEpic(epic1.getId());
            taskService.getEpic(epic2.getId());
            taskService.getEpic(epic3.getId());
            taskService.getSubtask(subtask1.getId());
            taskService.getSubtask(subtask2.getId());
            taskService.getSubtask(subtask3.getId());
            taskService.addTask(task5);
            taskService.getTask(task5.getId());
            List<Task> test = taskService.getHistory();
            test.forEach(System.out::println);
            for (Task task : test) {
                System.out.println(task.isViewed());
            }
            taskDao.updateTask(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}