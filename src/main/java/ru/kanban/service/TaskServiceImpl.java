package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import ru.kanban.dao.TaskDao;
import ru.kanban.exceptions.DaoException;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.*;
import ru.kanban.validator.TaskValidator;

import static ru.kanban.model.Status.*;
import static ru.kanban.model.TaskType.*;

public class TaskServiceImpl implements TaskService {
    private final TaskDao taskDao;
    private final HistoryService historyService;
    private final TaskValidator validator;

    public TaskServiceImpl(TaskDao taskDao, HistoryService historyService, TaskValidator validator) {
        this.taskDao = taskDao;
        this.historyService = historyService;
        this.validator = validator;
    }

    @Override
    public List<Task> getHistory() {
        return historyService.getViewedTasks();
    }

    @Override
    public Task addTask(Task task) {
        validator.validateTaskByType(task, TASK);
        taskDao.addTask(task);
        return task;
    }

    @Override
    public Optional<Task> getTask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            checkExists(id, TASK);
            Optional<Task> task = taskDao.getTask(id);
            addToHistory(task);
            return task;
        });
    }

    @Override
    public List<Task> getTasks() {
        return wrapTransaction(() -> {
            List<Task> result = taskDao.getTasks();
            historyService.addAll(result);
            return result;
        });
    }

    @Override
    public Optional<Task> deleteTask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            checkExists(id, TASK);
            Optional<Task> result = taskDao.deleteTask(id);
            historyService.remove(id);
            return result;
        });
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        validator.validateTaskByType(task, TASK);
        return wrapTransaction(() -> {
            checkExists(task.getId(), TASK);
            Optional<Task> result = taskDao.updateTask(task);
            historyRemoveIfViewed(task.isViewed(), task.getId());
            return result;
        });
    }

    @Override
    public void deleteAllTasks() {
        wrapTransaction(() -> {
            taskDao.deleteAllTasks();
            historyService.deleteAllByType(TASK.name());
            return null;
        });
    }

    @Override
    public Epic addEpic(Epic epic) {
        validator.validateTaskByType(epic, EPIC);
        taskDao.addEpic(epic);
        return epic;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        return wrapTransaction(() -> {
            validator.validateId(id);
            checkExists(id, EPIC);
            Optional<Epic> res = taskDao.getEpic(id);
            addToHistory(res);
            return res;
        });
    }

    @Override
    public List<Epic> getEpics() {
        return wrapTransaction(() -> {
            List<Epic> res = taskDao.getEpics();
            historyService.addAll(res);
            return res;
        });
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            checkExists(id, EPIC);
            Optional<Epic> res = taskDao.deleteEpic(id);
            historyService.remove(id);
            return res;
        });
    }

    @Override
    public void deleteAllEpics() {
        wrapTransaction(() -> {
            taskDao.deleteAllEpics();
            historyService.deleteAllByType(EPIC.name());
            historyService.deleteAllByType(SUBTASK.name());
            return null;
        });
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        validator.validateTaskByType(epic, EPIC);
        return wrapTransaction(() -> {
            checkExists(epic.getId(), EPIC);
            historyRemoveIfViewed(epic.isViewed(), epic.getId());
            taskDao.updateEpic(epic);
            updateEpicStatus(epic.getId());
            return taskDao.getEpic(epic.getId());
        });
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        validator.validateTaskByType(subtask, SUBTASK);
        return wrapTransaction(() -> {
            checkExists(subtask.getEpic().getId(), EPIC);
            taskDao.addSubtask(subtask);
            updateEpicStatus(subtask.getEpic().getId());
            return subtask;
        });
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            Optional<Subtask> result = taskDao.getSubtask(id);
            addToHistory(result);
            return result;
        });
    }

    @Override
    public List<Subtask> getSubtasks() {
        return wrapTransaction(() -> {
            List<Subtask> result = taskDao.getSubtasks();
            historyService.addAll(result);
            return result;
        });
    }

    @Override
    public Optional<Subtask> deleteSubtask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            checkExists(id, SUBTASK);
            Optional<Subtask> result = taskDao.getSubtask(id);
            Epic current = result.get().getEpic();
            taskDao.deleteSubtask(id);
            updateEpicStatus(current.getId());
            historyService.remove(id);
            return result;
        });
    }

    @Override
    public void deleteAllSubtasks() {
        wrapTransaction(() -> {
            taskDao.deleteAllSubtasks();
            taskDao.renewAllStatuses(EPIC.name(), NEW.name());
            historyService.deleteAllByType(SUBTASK.name());
            return null;
        });
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        validator.validateTaskByType(subtask, SUBTASK);
        return wrapTransaction(() -> {
            checkExists(subtask.getId(), SUBTASK);
            checkExists(subtask.getEpic().getId(), EPIC);
            historyRemoveIfViewed(subtask.isViewed(), subtask.getId());
            taskDao.updateSubtask(subtask);
            updateEpicStatus(subtask.getEpic().getId());
            return Optional.of(subtask);
        });
    }

    public Status checkSubtaskStatus(List<Status> statuses) {
        if (statuses.isEmpty()) {
            return NEW;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (Status actual : statuses) {
            if (!actual.equals(NEW)) {
                allNew = false;
            }

            if (!actual.equals(DONE)) {
                allDone = false;
            }

        }
        if (allNew) {
            return NEW;
        }

        if (allDone) {
            return DONE;
        }
        return IN_PROGRESS;
    }

    private void updateEpicStatus(int id) {
        var actualSubStatuses = taskDao.getEpicSubtasksStatuses(id);
        Status updatedStatus = checkSubtaskStatus(actualSubStatuses);
        taskDao.updateEpicStatus(id, updatedStatus);
    }

    private void addToHistory(Optional<? extends Task> task) {
        task.ifPresent(value -> {
            historyService.setToViewed(task.get());
            historyService.addToHistory(task.get());
        });
    }

    private <T> T wrapTransaction(Supplier<T> supplier) {
        taskDao.begin();
        try {
            T result = supplier.get();
            taskDao.commit();
            return result;
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    private void checkExists(int id, TaskType type) {
        if (!taskDao.existsById(id, type.name())) {
            throw new TaskNotFoundException(type.name() + " with id: " + id + " not found");
        }
    }

    private void historyRemoveIfViewed(boolean viewed, int id) {
        if (!viewed) {
            historyService.remove(id);
        }
    }
}
