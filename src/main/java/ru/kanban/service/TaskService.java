package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

public interface TaskService {
    List<Task> getHistory();

    Task addTask(Task task);

    Optional<Task> getTask(int id);

    List<Task> getTasks();

    Optional<Task> deleteTask(int id);

    Optional<Task> updateTask(Task task);

    void deleteAllTasks();

    Epic addEpic(Epic epic);

    Optional<Epic> getEpic(int id);

    List<Epic> getEpics();

    Optional<Epic> deleteEpic(int id);

    void deleteAllEpics();

    Optional<Epic> updateEpic(Epic epic);

    Subtask addSubtask(Subtask subtask);

    Optional<Subtask> getSubtask(int id);

    List<Subtask> getSubtasks();

    Optional<Subtask> deleteSubtask(int id);

    void deleteAllSubtasks();

    Optional<Subtask> updateSubtask(Subtask subtask);

}
