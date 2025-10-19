package ru.kanban.model;

import java.util.Objects;

public class Subtask extends Task {
    private Epic epic;
    private TaskType type;

    public Subtask(String name, String description, Status status, Epic epic) {
        super(name, description, status);
        this.epic = epic;
        this.type = TaskType.SUBTASK;
    }

    @Override
    public TaskType getType() {
        return type;
    }

    @Override
    public void setType(TaskType type) {
        this.type = type;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return (getId() == subtask.getId()) && getEpic().getId() == subtask.getEpic().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getEpic().getId());
    }

    @Override
    public String toString() {
        String ln = System.lineSeparator();
        return "  Subtask ID: " + getId() + ln +
                "    Name: '" + getName() + "'" + ln +
                "    Description: '" + getDescription() + "'" + ln +
                "    Status: '" + getStatus() + "'" + ln +
                "    Subtask of Epic: '" + epic.getId() + "'" + ln;
    }

}
