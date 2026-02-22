package ru.kanban.dao;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import ru.kanban.exceptions.ManagerSaveException;
import ru.kanban.model.*;

import static ru.kanban.model.Status.*;
import static ru.kanban.model.TaskType.*;
import static ru.kanban.service.TaskServiceImpl.log;
import static ru.kanban.utils.Constants.HEADER;

public class FileBackedTaskDao extends InMemoryTaskDao {
    private final String filePath;

    public FileBackedTaskDao(String path) {
        super();
        this.filePath = path;
    }

    public void save() {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(filePath), StandardCharsets.UTF_8))
        ) {
            writer.println(HEADER);
            for (Task task : getTasksWithoutAddingToHistory()) {
                writer.println(toString(task));
            }

            for (Epic epic : getEpicsWithoutAddingToHistory()) {
                writer.println(toString(epic));
            }

            for (Subtask subtask : getSubtasksWithoutAddingToHistory()) {
                writer.println(toString(subtask));
            }
        } catch (IOException e) {
            log.error("File is missing.");
            throw new ManagerSaveException("File writing exception");
        }
    }

    public void writeToFile(Task task) {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(filePath, true), StandardCharsets.UTF_8)
        )
        ) {
            if (Files.size(Path.of(filePath)) == 0) {
                writer.println(HEADER);
            }
            writer.println(toString(task));
        } catch (IOException e) {
            log.error("File is missing.");
            throw new ManagerSaveException("File writing exception");
        }
    }

    public String toString(Task task) {
        TaskType type = task.getType();
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), task.getType(), task.getName(), task.getStatus(), task.getDescription(),
                type.equals(SUBTASK) ? ((Subtask) task).getEpic().getId() : "");
    }

    public Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type;
        try {
            type = TaskType.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            log.error("Illegal task type, provided: {}", value);
            throw new IllegalArgumentException("Illegal task type.");
        }
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                Epic current = super.getEpicById(epicId);
                Subtask subtask = new Subtask(name, description, status, current);
                subtask.setId(id);
                return subtask;
            default:
                log.error("Unsupported task type: {}", value);
                throw new IllegalArgumentException("Unsupported task type.");
        }
    }

    public static FileBackedTaskDao loadFromFile(String[] args) throws FileNotFoundException {
        validateArgs(args);
        String taskPath = args[0];
        String historyPath = args[1];
        FileBackedHistoryDao historyManager = new FileBackedHistoryDao(historyPath);
        FileBackedTaskDao fileBackedTaskManager = new FileBackedTaskDao(
                taskPath
        );
        try (BufferedReader taskReader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(taskPath), StandardCharsets.UTF_8
                ));
             BufferedReader historyReader = new BufferedReader(new InputStreamReader(
                     new FileInputStream(historyPath), StandardCharsets.UTF_8
             ))
        ) {
            List<String> tasks = taskReader.lines().toList();
            List<String> history = historyReader.lines().toList();
            if (!tasks.get(0).equals(HEADER)) {
                log.error("File contains incorrect HEADER.");
                throw new IllegalArgumentException("Must be: id,type,name,status,description,epic id");
            }
            tasks.stream()
                    .skip(1)
                    .forEach(string -> {
                                validateFormat(string);
                                Task task = fileBackedTaskManager.fromString(string);
                                if (task.getType().equals(EPIC)) {
                                    fileBackedTaskManager.addEpicWithoutFileSaving((Epic) task);
                                    return;
                                }
                                if (task.getType().equals(SUBTASK)) {
                                    fileBackedTaskManager.addSubtaskWithoutSaving((Subtask) task);
                                } else {
                                    fileBackedTaskManager.addTaskWithoutFileSaving(task);
                                }
                            }
                    );
            history.stream()
                    .filter(string -> !string.isEmpty())
                    .forEach(string -> {
                                validateFormat(string);
                                Task task = fileBackedTaskManager.fromString(string);
                                if (task != null) {
                                    historyManager.addWithoutWrite(task);
                                }
                            }
                    );
        } catch (IOException e) {
            log.error("File is missing");
            throw new ManagerSaveException("File writing exception");
        }
        return fileBackedTaskManager;
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        writeToFile(task);
        return task;
    }

    @Override
    public Optional<Task> deleteTask(int id) {
        Optional<Task> res = super.deleteTask(id);
        save();
        return res;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        writeToFile(epic);
        return epic;
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        Optional<Epic> res = super.deleteEpic(id);
        save();
        return res;
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        Optional<Epic> res = super.updateEpic(epic);
        save();
        return res;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        writeToFile(subtask);
        return subtask;
    }

    @Override
    public boolean deleteSubtask(int id) {
        boolean res = super.deleteSubtask(id);
        save();
        return res;
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        Optional<Subtask> res = super.updateSubtask(subtask);
        save();
        return res;
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        Optional<Task> res = super.updateTask(task);
        save();
        return res;
    }

    private static void validateArgs(String[] args) throws FileNotFoundException {
        if (args.length < 2) {
            log.error("Not enough arguments, for execute. Enter paths: to TaskManager and History");
            throw new IllegalArgumentException(
                    "Not enough arguments, for execute. Enter paths: to TaskManager and History"
            );
        }
        if (!args[0].endsWith(".csv") || !args[1].endsWith(".csv")) {
            log.error("Illegal file extension. Expected : .csv");
            throw new IllegalArgumentException("Illegal file extension. Expected : .csv");
        }
        if (!Files.exists(Path.of(args[0]))) {
            log.error("Illegal file extension. Expected : .csv");
            throw new FileNotFoundException("File with tasks not found");
        }

        if (!Files.exists(Path.of(args[1]))) {
            log.error("File with history not found");
            throw new FileNotFoundException("File with history not found");
        }
    }

    private static void validateFormat(String string) {
        String[] parts = string.split(",");
        if (parts.length < 5 || parts.length > 6) {
            log.error("Must be: id,type,name,status,description,epic id");
            throw new IllegalArgumentException("Must be: id,type,name,status,description,epic id");
        }
        try {
            Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            log.error("Task ID is missing.");
            throw new IllegalArgumentException("Task ID is missing.");
        }
        boolean correctType = parts[1].equals(TASK.name())
                || parts[1].equals(EPIC.name())
                || parts[1].equals(SUBTASK.name());
        boolean correctName = !parts[2].isBlank();
        boolean correctStatus = parts[3].equals(NEW.name())
                || parts[3].equals(IN_PROGRESS.name())
                || parts[3].equals(DONE.name());
        boolean correctDescription = !parts[4].isBlank();

        if (!correctType) {
            log.error("Illegal task type.");
            throw new IllegalArgumentException("Illegal task type.");
        }
        if (!correctName) {
            log.error("Task name is missing");
            throw new IllegalArgumentException("Task name is missing");
        }
        if (!correctStatus) {
            log.error("Illegal status provided");
            throw new IllegalArgumentException("Illegal status provided");
        }

        if (!correctDescription) {
            log.error("Description is missing");
            throw new IllegalArgumentException("Description is missing");
        }

        if (parts[1].equals(SUBTASK.name())) {
            try {
                String epicId = parts[5];
                Integer.parseInt(epicId);
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("Epic ID at subtask: {}, is missing", parts[2]);
                throw new IllegalArgumentException("Epic ID at subtask: " + parts[2] + " is missing");
            } catch (NumberFormatException e) {
                log.error("Epic ID at subtask: {}, is invalid", parts[2]);
                throw new IllegalArgumentException("Epic ID at subtask: " + parts[2] + " is invalid");
            }
        }
    }

    private void addTaskWithoutFileSaving(Task task) {
        super.addTask(task);
    }

    private void addEpicWithoutFileSaving(Epic epic) {
        super.addEpic(epic);
    }

    private void addSubtaskWithoutSaving(Subtask subtask) {
        super.addSubtask(subtask);
    }

}
