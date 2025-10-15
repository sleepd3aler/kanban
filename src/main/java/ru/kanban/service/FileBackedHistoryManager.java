package ru.kanban.service;

import java.io.*;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
import ru.kanban.model.TaskType;

import static ru.kanban.model.TaskType.SUBTASK;

public class FileBackedHistoryManager extends InMemoryHistoryManager {
    private final File historyFile;

    public FileBackedHistoryManager(File file) {
        this.historyFile = file;
    }

    @Override
    public void addToHistory(Task task) {
        super.addToHistory(task);
        try (PrintWriter historyWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(historyFile, true))
        )) {
            historyWriter.println(toString(task));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addWithoutWrite(Task task) {
        super.addToHistory(task);
    }

    private String toString(Task task) {
        TaskType type = task.getType();
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), task.getType(), task.getName(), task.getStatus(), task.getDescription(),
                type.equals(SUBTASK) ? ((Subtask) task).getEpic().getId() : "");
    }

    public File getHistoryFile() {
        return historyFile;
    }

}
