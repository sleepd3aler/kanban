package ru.kanban.model;

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
    public String toString() {
        String ln = System.lineSeparator();
        return "  Subtask ID: " + getId() + ln +
                "    Name: '" + getName() + "'" + ln +
                "    Description: '" + getDescription() + "'" + ln +
                "    Status: '" + getStatus() + "'" + ln +
                "    Subtask of Epic: '" + epic.getName() + "'" + ln;
    }

}
