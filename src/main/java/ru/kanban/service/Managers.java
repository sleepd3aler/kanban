package ru.kanban.service;

public class Managers {
    private Managers() {

    }

    public static TaskManager getDefaultTaskManager(HistoryManager historyManager) {
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
