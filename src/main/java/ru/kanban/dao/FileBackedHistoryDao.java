package ru.kanban.dao;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kanban.exceptions.ManagerSaveException;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
import ru.kanban.model.TaskType;

import static ru.kanban.model.TaskType.SUBTASK;

public class FileBackedHistoryDao extends InMemoryHistoryDao {
    private static final Logger log = LoggerFactory.getLogger(FileBackedHistoryDao.class);
    private final String historyFile;

    public FileBackedHistoryDao(String path) {
        this.historyFile = path;
    }

    @Override
    public void addToHistory(Task task) {
        super.addToHistory(task);
        try (PrintWriter historyWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(historyFile, true))
        )) {
            historyWriter.println(toString(task));
        } catch (IOException e) {
            log.error("File is missing.");
            throw new ManagerSaveException("File writing exception");
        }
    }

    public void addWithoutWrite(Task task) {
        super.addToHistory(task);
    }

    public File getHistoryFile() {
        return new File(historyFile);
    }

    private String toString(Task task) {
        TaskType type = task.getType();
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), task.getType(), task.getName(), task.getStatus(), task.getDescription(),
                type.equals(SUBTASK) ? ((Subtask) task).getEpic().getId() : "");
    }

}
