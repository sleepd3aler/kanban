package ru.kanban.service;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kanban.dao.TaskDao;
import ru.kanban.exceptions.DaoException;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.*;
import ru.kanban.validator.TaskValidator;

import static ru.kanban.model.Status.*;
import static ru.kanban.model.TaskType.*;

public class TaskServiceImpl implements TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

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
        log.info("Task with ID: {}, added.", task.getId());
        return task;
    }

    @Override
    public Task getTask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            Task task = checkExists(id, TASK);
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
    public Task deleteTask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            Task result = checkExists(id, TASK);
            taskDao.deleteTask(id);
            historyService.remove(id);
            log.info("Task with ID : {} deleted.", id);
            return result;
        });
    }

    @Override
    public Task updateTask(Task task) {
        validator.validateTaskByType(task, TASK);
        return wrapTransaction(() -> {
            checkExists(task.getId(), TASK);
            taskDao.updateTask(task);
            historyRemoveIfViewed(task.isViewed(), task.getId());
            log.info("Task with ID: {} was changed. Actual name: {}, status: {}",
                    task.getId(),
                    task.getName(),
                    task.getStatus());
            return task;
        });
    }

    @Override
    public void deleteAllTasks() {
        wrapTransaction(() -> {
            taskDao.deleteAllTasks();
            historyService.deleteAllByType(TASK.name());
            log.info("All Tasks were deleted.");
            return null;
        });
    }

    @Override
    public Epic addEpic(Epic epic) {
        validator.validateTaskByType(epic, EPIC);
        taskDao.addEpic(epic);
        log.info("Epic with ID: {}, added.", epic.getId());
        return epic;
    }

    @Override
    public Epic getEpic(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            Epic res = (Epic) checkExists(id, EPIC);
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
    public Epic deleteEpic(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            Epic res = (Epic) checkExists(id, EPIC);
            taskDao.deleteEpic(id);
            historyService.remove(id);
            log.info("Epic with ID: {}, deleted.", id);
            return res;
        });
    }

    @Override
    public void deleteAllEpics() {
        wrapTransaction(() -> {
            taskDao.deleteAllEpics();
            historyService.deleteAllByType(EPIC.name());
            historyService.deleteAllByType(SUBTASK.name());
            log.info("All Epics were deleted.");
            return null;
        });
    }

    @Override
    public Epic updateEpic(Epic epic) {
        validator.validateTaskByType(epic, EPIC);
        return wrapTransaction(() -> {
            checkExists(epic.getId(), EPIC);
            taskDao.updateEpic(epic);
            historyRemoveIfViewed(epic.isViewed(), epic.getId());
            updateEpicStatus(epic.getId());
            log.info("Epic with ID: {} was changed. Actual name: {}, status: {}",
                    epic.getId(),
                    epic.getName(),
                    epic.getStatus());
            return taskDao.getEpic(epic.getId()).get();

        });
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        validator.validateTaskByType(subtask, SUBTASK);
        return wrapTransaction(() -> {
            checkEpicExists(subtask.getEpic().getId(), EPIC);
            taskDao.addSubtask(subtask);
            updateEpicStatus(subtask.getEpic().getId());
            log.info("Subtask with ID: {}, added.", subtask.getId());
            return subtask;
        });
    }

    @Override
    public Subtask getSubtask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            Subtask result = (Subtask) checkExists(id, SUBTASK);
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
    public Subtask deleteSubtask(int id) {
        validator.validateId(id);
        return wrapTransaction(() -> {
            Subtask result = (Subtask) checkExists(id, SUBTASK);
            Epic current = result.getEpic();
            taskDao.deleteSubtask(id);
            updateEpicStatus(current.getId());
            historyService.remove(id);
            log.info("Subtask with ID: {}, deleted.", id);
            return result;
        });
    }

    @Override
    public void deleteAllSubtasks() {
        wrapTransaction(() -> {
            taskDao.deleteAllSubtasks();
            taskDao.renewAllStatuses(EPIC.name(), NEW.name());
            historyService.deleteAllByType(SUBTASK.name());
            log.info("All Subtasks were deleted.");
            return null;
        });
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        validator.validateTaskByType(subtask, SUBTASK);
        return wrapTransaction(() -> {
            checkExists(subtask.getId(), SUBTASK);
            checkEpicExists(subtask.getEpic().getId(), EPIC);
            taskDao.updateSubtask(subtask);
            historyRemoveIfViewed(subtask.isViewed(), subtask.getId());
            updateEpicStatus(subtask.getEpic().getId());
            log.info("Subtask with ID: {} was changed. Actual name: {}, status: {}",
                    subtask.getId(),
                    subtask.getName(),
                    subtask.getStatus());
            return subtask;
        });
    }

    public Status checkEpicStatus(List<Status> statuses) {
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
        Status updatedStatus = this.checkEpicStatus(actualSubStatuses);
        taskDao.updateEpicStatus(id, updatedStatus);
        log.info("Epic with ID : {}, status updated to : {}", id, updatedStatus);
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
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            taskDao.rollback();
            log.error("Database connection failure :", e);
            throw new DaoException("Database connection failure: ", e);
        }
    }

    private void checkEpicExists(int id, TaskType type) {
        if (!taskDao.existsById(id, type.name())) {
            throw new TaskNotFoundException(type.name() + " with id: " + id + " not found");
        }
    }

    private void historyRemoveIfViewed(boolean viewed, int id) {
        if (!viewed) {
            historyService.remove(id);
        }
    }

    private Task checkExists(int id, TaskType type) {
        return switch (type) {
            case TASK -> taskDao.getTask(id)
                    .orElseThrow(() -> new TaskNotFoundException(type + " with id: " + id + " not found"));

            case EPIC -> taskDao.getEpic(id)
                    .orElseThrow(() -> new TaskNotFoundException(type + " with id: " + id + " not found"));

            case SUBTASK -> taskDao.getSubtask(id)
                    .orElseThrow(() -> new TaskNotFoundException(type + " with id: " + id + " not found"));
        };
    }
}
