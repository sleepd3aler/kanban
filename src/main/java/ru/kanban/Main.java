package ru.kanban;

import java.util.List;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.service.InMemoryTaskManager;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Epic test = new Epic("Epic1", "123", Status.NEW);
        taskManager.addEpic(test);
        Subtask sub1 = new Subtask("Epic1sub", "123", Status.NEW, new Epic("123", "123", Status.NEW));
        Subtask sub2 = new Subtask("Epic1sub2", "123", Status.NEW, test);
        taskManager.addSubtask(sub1);
        taskManager.addSubtask(sub2);
        System.out.println(test);
        List<Subtask> subtasks = test.getSubtasks();
//            taskManager.deleteSubtask(2);
            subtasks.forEach(System.out::println);
            taskManager.updateSubtask(sub1);
    }
}