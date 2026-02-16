package ru.kanban.validator;

import ru.kanban.model.Task;
import ru.kanban.model.TaskType;

import static ru.kanban.model.TaskType.TASK;

public class TaskValidator {

    public void validateTask(Task task, TaskType type) {
        if (task == null) {
            throw new IllegalArgumentException(type + " not null required");
        }
        if (task.getName().isEmpty()) {
            throw new IllegalArgumentException(type + " must contain name");
        }

        if (!task.getType().equals(type)) {
            throw new IllegalArgumentException("Illegal type provided, must be: " + TASK.name());
        }
    }

    public void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID value: cant be zero");
        }
    }
}
