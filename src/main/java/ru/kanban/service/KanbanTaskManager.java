package ru.kanban.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

public class KanbanTaskManager implements TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private int taskIds = 1;
    private int epicIds = 1;
    private int subtaskIds = 1;

    @Override
    public void addTask(Task task) {
        task.setId(taskIds++);
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
    public boolean updateTask(Task task, int id) {
        boolean result = tasks.containsKey(id);
        if (result) {
            task.setId(id);
            tasks.put(id, task);
            return true;
        }
        return false;
    }

    @Override
    public void deleteAllTasks() {
        this.tasks.clear();
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(epicIds++);
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
    }

    @Override
    public boolean updateEpic(Epic epic, int id) {
        boolean result = epics.containsKey(id);
        if (result) {
            epics.put(id, epic);
        }
        return result;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epic = getEpic(subtask.getEpic().getId());
        if (epic == null) {
            return;
        }
        subtask.setId(subtaskIds++);
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
        Epic epic = getEpic(id);
        if (epic != null) {
            epic.getSubtasks().removeIf(subtask -> subtask.getId() == id);
            updateStatus(epic);
        }
        return subtasks.remove(id) != null;
    }

    @Override
    public void deleteAllSubtasks() {
        this.subtasks.clear();
    }

    @Override
    public boolean updateSubtask(Subtask subtask, int id) {
        boolean result = subtasks.containsKey(id);
        if (result) {
            subtasks.put(id, subtask);
        }
        return result;
    }

    @Override
    public void updateStatus(Epic epic) {
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

