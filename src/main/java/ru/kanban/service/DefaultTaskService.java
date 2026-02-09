package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import ru.kanban.dao.TaskDao;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
import static ru.kanban.utils.Constants.*;

public class DefaultTaskService implements TaskService {
    private TaskDao taskDao;
    private HistoryService historyService;

    public DefaultTaskService(TaskDao taskDao, HistoryService historyService) {
        this.taskDao = taskDao;
        this.historyService = historyService;
    }

    @Override
    public List<Task> getHistory() {
        return historyService.getViewedTasks();
    }

    @Override
    public Task addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task not null required");
        }
        taskDao.addTask(task);
        return task;
    }

    @Override
    public Optional<Task> getTask(int id) {
        Optional<Task> task = taskDao.getTask(id);
        task.ifPresent(value -> {
            historyService.setToViewed(value);
            historyService.addToHistory(value);
        });
        return task;
    }

    @Override
    public List<Task> getTasks() {
        List<Task> result = taskDao.getTasks();
        historyService.addAll(result);
        return result;
    }

    @Override
    public Optional<Task> deleteTask(int id) {
        Optional<Task> task = taskDao.deleteTask(id);
        if (task.isEmpty()) {
            throw new TaskNotFoundException("Task with id: " + id + " not found");
        }
        historyService.remove(id);
        return task;
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task not null required");
        }
        Optional<Task> res = taskDao.getTask(task.getId());
        if (res.isEmpty()) {
            throw new TaskNotFoundException("Task with id: " + task.getId() + " not found");
        }
        if (!task.isViewed()) {
            historyService.remove(task.getId());
        }
        taskDao.updateTask(task);
        return Optional.of(task);
    }

    @Override
    public void deleteAllTasks() {
        taskDao.deleteAllTasks();
        historyService.deleteAllByType(TASK_TYPE);
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Epic not null required");
        }
        taskDao.addEpic(epic);
        return epic;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Optional<Epic> res = taskDao.getEpic(id);
        res.ifPresent(value -> {
            historyService.setToViewed(res.get());
            historyService.addToHistory(res.get());
        });
        return res;
    }

    @Override
    public List<Epic> getEpics() {
        List<Epic> result = taskDao.getEpics();
        historyService.addAll(result);
        return result;
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        Optional<Epic> res = taskDao.deleteEpic(id);
        if (res.isEmpty()) {
            throw new TaskNotFoundException("Epic with id: " + id + " not found");
        }
        historyService.remove(id);
        return res;
    }

    @Override
    public void deleteAllEpics() {
        taskDao.deleteAllEpics();
        historyService.deleteAllByType(EPIC_TYPE);
        historyService.deleteAllByType(SUBTASK_TYPE);
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Epic not null required");
        }
        Optional<Epic> res = taskDao.getEpic(epic.getId());
        if (res.isEmpty()) {
            throw new TaskNotFoundException("Epic with id: " + epic.getId() + " not found");
        }
        if (!epic.isViewed()) {
            historyService.remove(epic.getId());
        }

        taskDao.updateEpic(epic);
        taskDao.updateEpicStatus(epic.getId());
        return taskDao.getEpic(epic.getId());
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask not null required");
        }
        Optional<Epic> epic = taskDao.getEpic(subtask.getEpic().getId());
        if (epic.isEmpty()) {
            throw new TaskNotFoundException("Epic with id: " + subtask.getEpic().getId() + " not found");
        }
        taskDao.addSubtask(subtask);
        return subtask;
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Optional<Subtask> subtask = taskDao.getSubtask(id);
        subtask.ifPresent(value -> {
            historyService.setToViewed(value);
            historyService.addToHistory(value);
        });
        return subtask;
    }

    @Override
    public List<Subtask> getSubtasks() {
        List<Subtask> result = taskDao.getSubtasks();
        historyService.addAll(result);
        return result;
    }

    @Override
    public Optional<Subtask> deleteSubtask(int id) {
        Optional<Subtask> subtask = taskDao.getSubtask(id);
        if (subtask.isEmpty()) {
            throw new TaskNotFoundException("Subtask with id: " + id + " not found");
        }
        taskDao.updateEpicStatus(subtask.get().getEpic().getId());
        taskDao.deleteSubtask(id);
        historyService.remove(id);
        return subtask;
    }

    @Override
    public void deleteAllSubtasks() {
        taskDao.deleteAllSubtasks();
        historyService.deleteAllByType(SUBTASK_TYPE);
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask not null required");
        }
        Optional<Subtask> res = taskDao.getSubtask(subtask.getId());
        Optional<Epic> epicOfSubtask = taskDao.getEpic(subtask.getEpic().getId());
        if (res.isEmpty()) {
            throw new TaskNotFoundException("Subtask with id: " + subtask.getId() + " not found");
        }
        if (epicOfSubtask.isEmpty()) {
            throw new TaskNotFoundException("Epic with id: " + subtask.getEpic().getId() + " not found");
        }
        if (!subtask.isViewed()) {
            historyService.remove(subtask.getId());
        }
        taskDao.updateEpicStatus(epicOfSubtask.get().getId());
        taskDao.updateSubtask(subtask);
        return Optional.of(subtask);
    }
}
