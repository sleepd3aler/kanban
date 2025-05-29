package ru.kanban.service;

import java.util.List;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

public interface TaskManager {
    void addTask(Task task);

    Task getTask(int id);

    List<Task> getTasks();

    boolean deleteTask(int id);

    boolean updateTask(Task task, int id);

    void deleteAllTasks();

    void addEpic(Epic epic);

    Epic getEpic(int id);

    List<Epic> getEpics();

    boolean deleteEpic(int id);

    void deleteAllEpics();

    boolean updateEpic(Epic epic, int id);

    void addSubtask(Subtask subtask);

    Subtask getSubtask(int id);

    List<Subtask> getSubtasks();

    boolean deleteSubtask(int id);

    void deleteAllSubtasks();

    boolean updateSubtask(Subtask subtask, int id);

    void updateStatus(Epic epic);

}
