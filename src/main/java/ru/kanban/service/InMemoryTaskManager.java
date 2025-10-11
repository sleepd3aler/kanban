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
    private HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        task.setId(ids++);
        tasks.put(task.getId(), task);
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addToHistory(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<Task> deleteTask(int id) {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException("Task with id: " + id + " not found");
        }
        return Optional.of(tasks.remove(id));
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new TaskNotFoundException("Task with id: " + task.getId() + " not found");
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
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.addToHistory(epic);
        }
        return Optional.ofNullable(epic);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            throw new TaskNotFoundException("Epic with id: " + id + " not found");
        }
        epics.get(id).getSubtasks().forEach(subtask -> subtasks.remove(subtask.getId()));
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
        epic.updateStatus();
        epics.put(epic.getId(), epic);
        return Optional.of(epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epic = subtask.getEpic();
        if (epic == null || !epics.containsKey(epic.getId())) {
            throw new TaskNotFoundException("Epic with id: " + subtask.getEpic().getId() + " not found");
        }
        subtask.setId(ids++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.addToHistory(subtask);
        }
        return Optional.ofNullable(subtask);
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
        Epic epicOfSubtask = subtasks.get(id).getEpic();
        Subtask subtask = subtasks.remove(id);
        epicOfSubtask.getSubtasks().remove(subtask);
        epicOfSubtask.updateStatus();
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
        Epic epicOfSubtask = epics.get(subtask.getEpic().getId());
        for (int i = 0; i < epicOfSubtask.getSubtasks().size(); i++) {
            if (epicOfSubtask.getSubtasks().get(i).getId() == subtask.getId()) {
                epicOfSubtask.getSubtasks().set(i, subtask);
                break;
            }
        }
        epicOfSubtask.updateStatus();
        return Optional.of(subtask);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getViewedTasks());
    }
}


