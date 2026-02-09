package ru.kanban.utils;

import java.sql.Connection;
import ru.kanban.dao.*;

public class Managers {
    private Managers() {

    }

    public static TaskDao getDefaultTaskManager() {
        return new InMemoryTaskDao();
    }

    public static HistoryDao getDefaultHistoryManager() {
        return new InMemoryHistoryDao();
    }

    public static FileBackedHistoryDao getDefaultFileBackedHistoryManager(String string) {
        return new FileBackedHistoryDao(string);
    }

    public static FileBackedTaskDao getDefaultFileBackedManager(String path) {
        return new FileBackedTaskDao(path);
    }

    public static DbHistoryDao getDbHistoryManager(Connection connection) {
        return new DbHistoryDao(connection);
    }

    public static DbTaskDao getDbManager(Connection connection) {
    return new DbTaskDao(connection);
    }
}
