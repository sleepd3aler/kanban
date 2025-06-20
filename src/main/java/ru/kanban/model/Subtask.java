package ru.kanban.model;

import java.util.Objects;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(String name, String description, Status status, Epic epic) {
        super(name, description, status);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(epic, subtask.epic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epic);
    }

    @Override
    public String toString() {
        String ln = System.lineSeparator();
        return "  Subtask ID: " + getId() + ln +
                "    Name: '" + getName() + "'" + ln +
                "    Description: '" + getDescription() + "'" + ln +
                "    Status: '" + getStatus() + "'" + ln +
                "    Subtask of Epic: '" + epic.getName() + "'" + ln;
    }

}
