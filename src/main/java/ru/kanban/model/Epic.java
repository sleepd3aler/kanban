package ru.kanban.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        updateStatus();
    }

    public void updateStatus() {
        boolean allNew = true;
        boolean allDone = true;
        for (Subtask subtask : this.getSubtasks()) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }
        if (this.getSubtasks().isEmpty() || allNew) {
            this.setStatus(Status.NEW);
        } else if (allDone) {
            this.setStatus(Status.DONE);
        } else {
            this.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
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
