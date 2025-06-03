package ru.kanban.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        if (subtasks.contains(subtask)) {
            return;
        }
        subtasks.add(subtask);
    }

    public static void updateStatus(Epic epic) {
        boolean allNew = true;
        boolean allDone = true;
        for (Subtask subtask : epic.getSubtasks()) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }
        if (epic.getSubtasks().isEmpty() || allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public String toString() {
        String ln = System.lineSeparator();
        return "Epic {" + " ID: " + getId() + ", Name: '" + getName() + "', Description: '" + getDescription() + "'" +
                " Status: '" + getStatus() + "' }" +
                ln +
                "Subtasks: " + subtasks +
                '}';
    }
}
