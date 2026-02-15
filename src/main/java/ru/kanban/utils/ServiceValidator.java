package ru.kanban.utils;

import ru.kanban.model.Task;

public class ServiceValidator {

    public static void validateTask(Task task, String typeMsg) {
        if (task == null) {
            throw new IllegalArgumentException(typeMsg + " not null required");
        }
        if (task.getName().isEmpty()) {
            throw new IllegalArgumentException(typeMsg + " must contain name");
        }
    }

    public static void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID value: cant be zero");
        }
    }
}
