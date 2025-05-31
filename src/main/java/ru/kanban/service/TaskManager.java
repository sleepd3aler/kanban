package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

public interface TaskManager {
    void addTask(Task task);

    Task getTask(int id);

    List<Task> getTasks();

    boolean deleteTask(int id);

    Optional<Task> updateTask(Task task);

    void deleteAllTasks();

    void addEpic(Epic epic);

    Epic getEpic(int id);

    List<Epic> getEpics();

    boolean deleteEpic(int id);

    void deleteAllEpics();

    Optional<Epic> updateEpic(Epic epic);

    void addSubtask(Subtask subtask);

    Subtask getSubtask(int id);

    List<Subtask> getSubtasks();

    boolean deleteSubtask(int id);

    void deleteAllSubtasks();

    Optional<Subtask> updateSubtask(Subtask subtask);

}
