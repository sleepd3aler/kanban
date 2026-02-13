package ru.kanban.dao;

import java.util.*;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static ru.kanban.model.Status.NEW;
import static ru.kanban.utils.Constants.*;

public class InMemoryTaskDao implements TaskDao {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private int ids = 1;

    @Override
    public Task addTask(Task task) {
        task.setId(ids++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        return Optional.ofNullable(task);
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<Task> deleteTask(int id) {
        return Optional.of(tasks.remove(id));
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        tasks.put(task.getId(), task);
        return Optional.of(task);
    }

    @Override
    public void deleteAllTasks() {
        this.tasks.clear();
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(ids++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        return Optional.ofNullable(epic);
    }

    @Override
    public List<Epic> getEpics() {
        List<Epic> result = new ArrayList<>(epics.values());
        result.forEach(Epic -> Epic.setViewed(true));
        return result;
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        epics.get(id).getSubtasks()
                .forEach(subtask -> subtasks.remove(subtask.getId()));
        return Optional.of(epics.remove(id));
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    @Override
    public void deleteAllEpics() {
        this.epics.clear();
        this.subtasks.clear();
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return Optional.of(epic);
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = subtask.getEpic();
        subtask.setId(ids++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        return subtask;
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        return Optional.ofNullable(subtask);
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public boolean deleteSubtask(int id) {
        Epic epicOfSubtask = subtasks.get(id).getEpic();
        Subtask subtask = subtasks.remove(id);
        epicOfSubtask.getSubtasks().remove(subtask);
        return subtask != null;
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epicOfSubtask = epics.get(subtask.getEpic().getId());
        for (int i = 0; i < epicOfSubtask.getSubtasks().size(); i++) {
            if (epicOfSubtask.getSubtasks().get(i).getId() == subtask.getId()) {
                epicOfSubtask.getSubtasks().set(i, subtask);
                break;
            }
        }
        return Optional.of(subtask);
    }

    @Override
    public void updateEpicStatus(int id, Status status) {
        epics.get(id).setStatus(status);
    }

    // Можно подумать о более функциональном методе - обновляем все задачи
    // определенным статусом по типу

    @Override
    public void renewAllStatuses(String type, String status) {
        switch (type) {
            case TASK_TYPE -> tasks.values()
                    .forEach(task -> task.setStatus(Status.valueOf(status)));
            case EPIC_TYPE -> epics.values()
                    .forEach(epic -> {
                        epic.setStatus(Status.valueOf(status));
                        if (status.equals(NEW.name())) {
                            epic.getSubtasks().clear();
                        }
                    });

            case SUBTASK_TYPE -> subtasks.values()
                    .forEach(subtask -> subtask.setStatus(Status.valueOf(status)));
        }
    }

    @Override
    public List<Status> getEpicSubtasksStatuses(int epicId) {
        return epics.get(epicId)
                .getSubtasks()
                .stream()
                .map(Task::getStatus)
                .toList();
    }

    public List<Task> getTasksWithoutAddingToHistory() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getEpicsWithoutAddingToHistory() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getSubtasksWithoutAddingToHistory() {
        return new ArrayList<>(subtasks.values());
    }

}
