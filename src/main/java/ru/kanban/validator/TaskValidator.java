package ru.kanban.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kanban.model.Task;
import ru.kanban.model.TaskType;

import static ru.kanban.model.TaskType.TASK;

public class TaskValidator {

    private static final Logger log = LoggerFactory.getLogger(TaskValidator.class);

    public void validateTaskByType(Task task, TaskType type) {
        if (task == null) {
            log.error("{} not null required", type);
            throw new IllegalArgumentException(type + " not null required");
        }
        if (!task.getType().equals(type)) {
            log.error("Illegal type provided, must be: {}", TASK.name());
            throw new IllegalArgumentException("Illegal type provided, must be: " + TASK.name());
        }
        if (task.getName().isEmpty()) {
            log.error("{} must contain name", type);
            throw new IllegalArgumentException(type + " must contain name");
        }

    }

    public void validateId(int id) {
        if (id <= 0) {
            log.error("ID value: cant be zero");
            throw new IllegalArgumentException("ID value: cant be zero");
        }
    }
}
