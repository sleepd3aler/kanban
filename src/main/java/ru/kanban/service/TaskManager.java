package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import ru.kanban.exceptions.EpicNotFoundException;
import ru.kanban.exceptions.SubtaskNotFoundException;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

public interface TaskManager {
    void addTask(Task task);

    Task getTask(int id);

    List<Task> getTasks();

    Optional<Task> deleteTask(int id) throws TaskNotFoundException;

    Optional<Task> updateTask(Task task) throws TaskNotFoundException;

    void deleteAllTasks();

    void addEpic(Epic epic);

    Epic getEpic(int id);

    List<Epic> getEpics();

    Optional<Epic> deleteEpic(int id) throws EpicNotFoundException;

    void deleteAllEpics();

    Optional<Epic> updateEpic(Epic epic) throws EpicNotFoundException;

    void addSubtask(Subtask subtask);

    Subtask getSubtask(int id);

    List<Subtask> getSubtasks();

    Optional<Subtask> deleteSubtask(int id) throws SubtaskNotFoundException;

    void deleteAllSubtasks();

    Optional<Subtask> updateSubtask(Subtask subtask) throws SubtaskNotFoundException;

}
