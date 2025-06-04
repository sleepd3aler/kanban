package ru.kanban.service;

import java.util.*;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();

    private int ids = 1;

    @Override
    public void addTask(Task task) {
        task.setId(ids++);
        tasks.put(task.getId(), task);
    }

    @Override
    public Task getTask(int id) {
        return tasks.get(id);
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<Task> deleteTask(int id) throws TaskNotFoundException {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException("Task with id " + id + " not found");
        }
        return Optional.of(tasks.remove(id));
    }

    @Override
    public Optional<Task> updateTask(Task task) throws TaskNotFoundException {
        if (!tasks.containsKey(task.getId())) {
            throw new TaskNotFoundException("Task with id " + task.getId() + " not found");
        }
        tasks.put(task.getId(), task);
        return Optional.of(task);
    }

    @Override
    public void deleteAllTasks() {
        this.tasks.clear();
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(ids++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            throw new TaskNotFoundException("Epic with id: " + id + " not found");
        }
        epics.get(id).getSubtasks().clear();
        return Optional.of(epics.remove(id));
    }

    @Override
    public void deleteAllEpics() {
        this.epics.clear();
        this.subtasks.clear();
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new TaskNotFoundException("Epic with id: " + epic.getId() + " not found");
        }
        epics.put(epic.getId(), epic);
        return Optional.of(epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epic = getEpic(subtask.getEpic().getId());
        if (epic == null) {
            throw new TaskNotFoundException("Epic with id " + subtask.getEpic().getId() + " not found");
        }
        subtask.setId(ids++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        epic.updateStatus();
    }

    @Override
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Optional<Subtask> deleteSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            throw new TaskNotFoundException("Subtask with id: " + id + " not found");
        }
        Subtask subtask = subtasks.remove(id);
        Epic epic = getEpic(subtask.getEpic().getId());
        epic.getSubtasks().remove(subtask);
        epic.updateStatus();
        return Optional.of(subtask);
    }

    @Override
    public void deleteAllSubtasks() {
        this.subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.updateStatus();
        }
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new TaskNotFoundException("Subtask with id " + subtask.getId() + " not found");
        }
        if (!epics.containsKey(subtask.getEpic().getId())) {
            throw new TaskNotFoundException("Epic with id: " + subtask.getEpic().getId() + " not found");
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = getEpic(subtask.getEpic().getId());
        epic.updateStatus();
        return Optional.of(subtask);
    }

}

