package ru.kanban.service;

import java.io.File;

public class Managers {
    private Managers() {

    }

    public static TaskManager getDefaultTaskManager(HistoryManager historyManager) {
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedHistoryManager getDefaultFileBackedHistoryManager(File file) {
        return new FileBackedHistoryManager(file);
    }
}
