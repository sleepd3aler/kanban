package ru.kanban.service;

import java.sql.Connection;

public class Managers {
    private Managers() {

    }

    public static TaskManager getDefaultTaskManager(HistoryManager historyManager) {
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedHistoryManager getDefaultFileBackedHistoryManager(String string) {
        return new FileBackedHistoryManager(string);
    }

    public static FileBackedTaskManager getDefaultFileBackedManager(String path, HistoryManager historyManager) {
        return new FileBackedTaskManager(path, historyManager);
    }

    public static DbHistoryManager getDbHistoryManager(Connection connection) {
        return new DbHistoryManager(connection);
    }

    public static DbTaskManager getDbManager(Connection connection, HistoryManager historyManager) {
    return new DbTaskManager(connection, historyManager);
    }
}
