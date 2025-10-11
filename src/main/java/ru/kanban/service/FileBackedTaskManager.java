package ru.kanban.service;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import ru.kanban.exceptions.ManagerSaveException;
import ru.kanban.model.*;

import static ru.kanban.model.TaskType.EPIC;
import static ru.kanban.model.TaskType.SUBTASK;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path path;

    public FileBackedTaskManager(Path path, HistoryManager historyManager) {
        super(historyManager);
        this.path = path;
    }

    public void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()))) {
            writer.println("id,type,name,status,description,epic");
            for (Task task : getTasks()) {
                writer.println(toString(task));
            }

            for (Epic epic : getEpics()) {
                writer.println(toString(epic));
            }

            for (Subtask subtask : getSubtasks()) {
                writer.println(toString(subtask));
            }

            writer.println("History: ");
            for (Task task : getHistory()) {
                writer.println(toString(task));
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public String toString(Task task) {
        TaskType type = task.getType();
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), task.getType(), task.getName(), task.getStatus(), task.getDescription(),
                type.equals(SUBTASK) ? ((Subtask) task).getEpic().getId() : "");
    }

    public Task fromString(String value) {
        if (value.isEmpty()) {
            return null;
        }
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
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
                if (current != null) {
                    Subtask subtask = new Subtask(name, description, status, current);
                    subtask.setId(id);
                    return subtask;
                }
            default:
                throw new IllegalArgumentException("Subtask: " + name + ", missing Epic");
        }

    }

    public static FileBackedTaskManager loadFromFile(File file) {
        HistoryManager historyManager = Managers.getDefaultHistoryManager();
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(
                file.toPath(),
                historyManager
        );
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> tasks = reader.lines().toList();
            tasks.stream()
                    .skip(1)
                    .takeWhile(string -> !string.startsWith("History: "))
                    .forEach(string -> {
                        Task task = fileBackedTaskManager.fromString(string);
                        if (task.getType().equals(EPIC)) {
                            fileBackedTaskManager.addEpic((Epic) task);
                            return;
                        }
                        if (task.getType().equals(SUBTASK)) {
                            fileBackedTaskManager.addSubtask((Subtask) task);
                        } else {
                            fileBackedTaskManager.addTask(task);
                        }
                    });

            tasks.stream()
                    .dropWhile(string -> !string.startsWith("History: "))
                    .skip(1)
                    .forEach(string -> historyManager.addToHistory(fileBackedTaskManager.fromString(string)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBackedTaskManager;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
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
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
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
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public Optional<Subtask> deleteSubtask(int id) {
        Optional<Subtask> res = super.deleteSubtask(id);
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

    @Override
    public Optional<Task> getTask(int id) {
        Optional<Task> res = super.getTask(id);
        save();
        return res;
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Optional<Subtask> res = super.getSubtask(id);
        save();
        return res;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Optional<Epic> res = super.getEpic(id);
        save();
        return res;
    }
}
