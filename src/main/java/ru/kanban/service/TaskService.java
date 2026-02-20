package ru.kanban.service;

import java.util.List;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

public interface TaskService {
    List<Task> getHistory();

    Task addTask(Task task);

    Task getTask(int id);

    List<Task> getTasks();

    Task deleteTask(int id);

    Task updateTask(Task task);

    void deleteAllTasks();

    Epic addEpic(Epic epic);

    Epic getEpic(int id);

    List<Epic> getEpics();

    Epic deleteEpic(int id);

    void deleteAllEpics();

    Epic updateEpic(Epic epic);

    Subtask addSubtask(Subtask subtask);

    Subtask getSubtask(int id);

    List<Subtask> getSubtasks();

    Subtask deleteSubtask(int id);

    void deleteAllSubtasks();

    Subtask updateSubtask(Subtask subtask);

    Status checkSubtaskStatus(List<Status> statuses);

}
