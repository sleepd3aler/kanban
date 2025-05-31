package ru.kanban.service;

import java.util.*;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
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
    public boolean deleteTask(int id) {
        return tasks.remove(id) != null;
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new NoSuchElementException("Task with id " + task.getId() + " not found");
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
    public boolean deleteEpic(int id) {
        return epics.remove(id) != null;
    }

    @Override
    public void deleteAllEpics() {
        this.epics.clear();
        this.subtasks.clear();
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NoSuchElementException("Epic with id " + epic.getId() + " not found");
        }
        epics.put(epic.getId(), epic);
        return Optional.of(epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epic = getEpic(subtask.getEpic().getId());
        if (epic == null) {
            return;
        }
        subtask.setId(ids++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        updateStatus(epic);
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
    public boolean deleteSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            return false;
        }
        Subtask subtask = subtasks.get(id);
        Epic epic = getEpic(subtask.getEpic().getId());
        epic.getSubtasks().remove(subtask);
        updateStatus(epic);
        return true;

//        Epic epic = getEpic(id);
//        if (epic != null) {
//            epic.getSubtasks().removeIf(subtask -> subtask.getId() == id);
//            updateStatus(epic);
//        }
//        return subtasks.remove(id) != null;
    }

    @Override
    public void deleteAllSubtasks() {
        this.subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            updateStatus(epic);
        }
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new NoSuchElementException("Subtask with id " + subtask.getId() + " not found");
        }
        subtasks.put(subtask.getId(), subtask);
        updateStatus(epics.get(subtask.getEpic().getId()));
        return Optional.of(subtask);
    }

    private void updateStatus(Epic epic) {
        boolean result = epic.getSubtasks().isEmpty() ||
                epic.getSubtasks()
                        .stream()
                        .allMatch(subtask -> subtask.getStatus() == Status.NEW);
        if (result) {
            epic.setStatus(Status.NEW);
            return;
        }
        result = epic.getSubtasks().stream().allMatch(subtask -> subtask.getStatus() == Status.DONE);
        if (result) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}

