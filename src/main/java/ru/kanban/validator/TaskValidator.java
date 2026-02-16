package ru.kanban.validator;

import ru.kanban.model.Task;

public class TaskValidator {

    public  void validateTask(Task task, String typeMsg) {
        if (task == null) {
            throw new IllegalArgumentException(typeMsg + " not null required");
        }
        if (task.getName().isEmpty()) {
            throw new IllegalArgumentException(typeMsg + " must contain name");
        }

        if (!task.getType().name().equals(typeMsg)) {
            throw new IllegalArgumentException("Illegal type provided, must be: " + typeMsg);
        }
    }

    public  void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID value: cant be zero");
        }
    }
}
