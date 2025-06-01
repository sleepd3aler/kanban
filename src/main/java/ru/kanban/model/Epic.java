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

    @Override
    public String toString() {
        String ln = System.lineSeparator();
        return "Epic {" + " ID: " + getId() + ", Name: '" + getName() + "', Description: '" + getDescription() + "'" +
                " Status: '" + getStatus() +  "' }" +
                ln +
                "Subtasks: " + subtasks +
                '}';
    }
}
