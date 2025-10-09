package ru.kanban.service;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import ru.kanban.exceptions.ManagerSaveException;
import ru.kanban.model.*;

import static ru.kanban.model.TaskType.*;

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
        TaskType taskType = task instanceof Epic ? EPIC
                : task instanceof Subtask ? SUBTASK
                : TASK;
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), taskType, task.getName(), task.getStatus(), task.getDescription(),
                task instanceof Subtask ? ((Subtask) task).getEpic().getId() : "");
    }

    public Task fromString(String value) {
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
                Optional<Epic> checkEpic = getEpic(epicId);
                if (checkEpic.isPresent()) {
                    Epic current = checkEpic.get();
                    Subtask subtask = new Subtask(name, description, status, current);
                    subtask.setId(id);
                    return subtask;
                }
            default:
                throw new IllegalArgumentException();
        }

    }

    public static FileBackedTaskManager loadFromFile(File file) {
        HistoryManager historyManager = Managers.getDefaultHistoryManager();
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(
                file.toPath(),
                historyManager
        );
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines()
                    .skip(1)
                    .takeWhile(string -> !string.startsWith("History"))
                    .forEach(string -> {
                        Task task = fileBackedTaskManager.fromString(string);
                        if (task instanceof Epic) {
                            fileBackedTaskManager.addEpic((Epic) task);
                            return;
                        }
                        if (task instanceof Subtask) {
                            fileBackedTaskManager.addSubtask((Subtask) task);
                        } else {
                            fileBackedTaskManager.addTask(task);
                        }
                    })
            ;
            reader.lines()
                    .dropWhile(string -> string.startsWith("History"))
                    .skip(1)
                    .forEach(string -> {
                                Task task = fileBackedTaskManager.fromString(string);
                                historyManager.addToHistory(task);
                            }
                    );
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

    public static void main(String[] args) throws IOException {
        File tempFile = File.createTempFile("temp", "csv");
        HistoryManager historyManager = Managers.getDefaultHistoryManager();
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile.toPath(), historyManager);
        Task task1 = new Task("Task 1", "Description 1", Status.IN_PROGRESS);
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        Epic epic1 = new Epic("Epic 1", "Description 1", Status.IN_PROGRESS);
        Epic epic2 = new Epic("Epic 2", "Description 2", Status.IN_PROGRESS);
        Subtask subtask1 = new Subtask("Subtask1", "Description1", Status.NEW, epic1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", Status.NEW, epic2);
        fileBackedTaskManager.addTask(task1);
        fileBackedTaskManager.addTask(task2);
        fileBackedTaskManager.addEpic(epic1);
        fileBackedTaskManager.addEpic(epic2);
        fileBackedTaskManager.addSubtask(subtask1);
        fileBackedTaskManager.addSubtask(subtask2);
        fileBackedTaskManager.getSubtask(5).get().setStatus(Status.DONE);
        fileBackedTaskManager.updateEpic(epic1);
        fileBackedTaskManager.getTask(1);
        fileBackedTaskManager.getTask(2);
        fileBackedTaskManager.getSubtask(6);
        fileBackedTaskManager.getEpic(3);
        fileBackedTaskManager.getHistory();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            reader.lines().forEach(System.out::println);
        }
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasks = loadedManager.getTasks();
        tasks.forEach(System.out::println);
        List<Epic> epics = loadedManager.getEpics();
        epics.forEach(System.out::println);
        List<Subtask> subtasks = loadedManager.getSubtasks();
        subtasks.forEach(System.out::println);
        loadedManager.getHistory().forEach(System.out::println);

    }
}
