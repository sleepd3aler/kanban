package ru.kanban.service;

import java.util.List;
import ru.kanban.model.Task;

public interface HistoryManager {
    void setToViewed(Task task);

    void addToHistory(Task task);

    List<Task> getViewedTasks();
}
