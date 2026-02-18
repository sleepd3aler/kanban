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
            Task task = taskDao.getTask(id)
                    .orElseThrow(() -> new TaskNotFoundException("TASK with id: " + id + "not found"));

            addToHistory(task);
            return Optional.of(task);
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
            Task result = taskDao.deleteTask(id)
                    .orElseThrow(() -> new TaskNotFoundException("TASK with id: " + id + " not found."));
            historyService.remove(id);
            return Optional.of(result);
        });
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        validator.validateTaskByType(task, TASK);
        return wrapTransaction(() -> {
            Task result = taskDao.updateTask(task)
                    .orElseThrow(() -> new TaskNotFoundException("TASK with id: " + task.getId() + " not found."));
            historyRemoveIfViewed(task.isViewed(), task.getId());
            return Optional.of(result);
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
            Epic res = taskDao.getEpic(id)
                    .orElseThrow(() -> new TaskNotFoundException("EPIC with id: " + id + " not found."));
            addToHistory(res);
            return Optional.of(res);
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
            Epic res = taskDao.deleteEpic(id)
                    .orElseThrow(() -> new TaskNotFoundException("EPIC with id: " + id + " not found."));
            historyService.remove(id);
            return Optional.of(res);
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
            taskDao.updateEpic(epic)
                    .orElseThrow(() -> new TaskNotFoundException("EPIC with id: " + epic.getId() + " not found."));
            historyRemoveIfViewed(epic.isViewed(), epic.getId());
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
            Subtask result = taskDao.getSubtask(id)
                    .orElseThrow(() -> new TaskNotFoundException("SUBTASK with id: " + id + " not found."));
            addToHistory(result);
            return Optional.of(result);
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
            Subtask result = taskDao.getSubtask(id)
                    .orElseThrow(() -> new TaskNotFoundException("SUBTASK with id: " + id + " not found."));
            Epic current = result.getEpic();
            taskDao.deleteSubtask(id);
            updateEpicStatus(current.getId());
            historyService.remove(id);
            return Optional.of(result);
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
            taskDao.updateSubtask(subtask)
                    .orElseThrow(() -> new TaskNotFoundException("SUBTASK with id: " + subtask.getId() + " not found."));
            checkExists(subtask.getEpic().getId(), EPIC);
            historyRemoveIfViewed(subtask.isViewed(), subtask.getId());
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

    private void addToHistory(Task task) {
        historyService.setToViewed(task);
        historyService.addToHistory(task);
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
