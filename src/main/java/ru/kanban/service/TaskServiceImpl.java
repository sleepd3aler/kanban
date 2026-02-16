package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import ru.kanban.dao.TaskDao;
import ru.kanban.exceptions.DaoException;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
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
        try {
            taskDao.begin();
            Optional<Task> task = taskDao.getTask(id);
            task.ifPresent(value -> {
                historyService.setToViewed(value);
                historyService.addToHistory(value);
            });
            taskDao.commit();
            return task;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }

    }

    @Override
    public List<Task> getTasks() {
        try {
            taskDao.begin();
            List<Task> result = taskDao.getTasks();
            historyService.addAll(result);
            taskDao.commit();
            return result;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Optional<Task> deleteTask(int id) {
        validator.validateId(id);
        try {
            if (!taskDao.existsById(id, TASK.name())) {
                throw new TaskNotFoundException("Task with id: " + id + " not found");
            }
            taskDao.begin();
            Optional<Task> task = taskDao.deleteTask(id);
            historyService.remove(id);
            taskDao.commit();
            return task;
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        validator.validateTaskByType(task, TASK);
        try {
            taskDao.begin();
            if (!taskDao.existsById(task.getId(), TASK.name())) {
                throw new TaskNotFoundException("Task with id: " + task.getId() + " not found");
            }
            if (!task.isViewed()) {
                historyService.remove(task.getId());
            }
            taskDao.updateTask(task);
            taskDao.commit();
            return Optional.of(task);
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public void deleteAllTasks() {
        try {
            taskDao.begin();
            taskDao.deleteAllTasks();
            historyService.deleteAllByType(TASK.name());
            taskDao.commit();
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Epic addEpic(Epic epic) {
        validator.validateTaskByType(epic, EPIC);
        taskDao.addEpic(epic);
        return epic;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        validator.validateId(id);
        try {
            taskDao.begin();
            Optional<Epic> res = taskDao.getEpic(id);
            res.ifPresent(value -> {
                historyService.setToViewed(res.get());
                historyService.addToHistory(res.get());
            });
            taskDao.commit();
            return res;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public List<Epic> getEpics() {
        try {
            taskDao.begin();
            List<Epic> result = taskDao.getEpics();
            historyService.addAll(result);
            taskDao.commit();
            return result;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        validator.validateId(id);
        try {
            taskDao.begin();
            if (!taskDao.existsById(id, EPIC.name())) {
                throw new TaskNotFoundException("Epic with id: " + id + " not found");
            }
            Optional<Epic> res = taskDao.deleteEpic(id);
            historyService.remove(id);
            taskDao.commit();
            return res;
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public void deleteAllEpics() {
        try {
            taskDao.begin();
            taskDao.deleteAllEpics();
            historyService.deleteAllByType(EPIC.name());
            historyService.deleteAllByType(SUBTASK.name());
            taskDao.commit();
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        validator.validateTaskByType(epic, EPIC);
        try {
            taskDao.begin();
            if (!taskDao.existsById(epic.getId(), EPIC.name())) {
                throw new TaskNotFoundException("Epic with id: " + epic.getId() + " not found");
            }
            if (!epic.isViewed()) {
                historyService.remove(epic.getId());
            }
            taskDao.updateEpic(epic);
            updateEpicStatus(epic.getId());
            taskDao.commit();
            return taskDao.getEpic(epic.getId());
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        validator.validateTaskByType(subtask, SUBTASK);
        try {
            taskDao.begin();
            if (!taskDao.existsById(subtask.getEpic().getId(), EPIC.name())) {
                throw new TaskNotFoundException("Epic with id: " + subtask.getEpic().getId() + " not found");
            }
            taskDao.addSubtask(subtask);
            updateEpicStatus(subtask.getEpic().getId());
            taskDao.commit();
            return subtask;
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        validator.validateId(id);
        try {
            taskDao.begin();
            Optional<Subtask> subtask = taskDao.getSubtask(id);
            subtask.ifPresent(value -> {
                historyService.setToViewed(value);
                historyService.addToHistory(value);
            });
            taskDao.commit();
            return subtask;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public List<Subtask> getSubtasks() {
        try {
            taskDao.begin();
            List<Subtask> result = taskDao.getSubtasks();
            historyService.addAll(result);
            taskDao.commit();
            return result;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Optional<Subtask> deleteSubtask(int id) {
        validator.validateId(id);
        try {
            taskDao.begin();
            if (!taskDao.existsById(id, SUBTASK.name())) {
                throw new TaskNotFoundException("Subtask with id: " + id + " not found");
            }
            Optional<Subtask> subtask = taskDao.getSubtask(id);
            Epic current = subtask.get().getEpic();
            taskDao.deleteSubtask(id);
            updateEpicStatus(current.getId());
            historyService.remove(id);
            taskDao.commit();
            return subtask;
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        try {
            taskDao.begin();
            taskDao.deleteAllSubtasks();
            taskDao.renewAllStatuses(EPIC.name(), NEW.name());
            historyService.deleteAllByType(SUBTASK.name());
            taskDao.commit();
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        validator.validateTaskByType(subtask, SUBTASK);
        try {
            taskDao.begin();
            if (!taskDao.existsById(subtask.getId(), SUBTASK.name())) {
                throw new TaskNotFoundException("Subtask with id: " + subtask.getId() + " not found");
            }
            if (!taskDao.existsById(subtask.getEpic().getId(), EPIC.name())) {
                throw new TaskNotFoundException("Epic with id: " + subtask.getEpic().getId() + " not found");
            }
            if (!subtask.isViewed()) {
                historyService.remove(subtask.getId());
            }
            taskDao.updateSubtask(subtask);
            updateEpicStatus(subtask.getEpic().getId());
            taskDao.commit();
            return Optional.of(subtask);
        } catch (TaskNotFoundException e) {
            taskDao.rollback();
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            throw new DaoException("Database connection failure: ", e);
        }
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
}
