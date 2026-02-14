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

import static ru.kanban.model.Status.*;
import static ru.kanban.model.TaskType.*;

public class TaskServiceImpl implements TaskService {
    private TaskDao taskDao;
    private HistoryService historyService;

    public TaskServiceImpl(TaskDao taskDao, HistoryService historyService) {
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
        if (task == null) {
            throw new IllegalArgumentException("Task not null required");
        }
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
        if (epic == null) {
            throw new IllegalArgumentException("Epic not null required");
        }
        taskDao.addEpic(epic);
        return epic;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
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
        if (epic == null) {
            throw new IllegalArgumentException("Epic not null required");
        }
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
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask not null required");
        }
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
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask not null required");
        }
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
