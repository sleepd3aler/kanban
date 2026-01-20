package ru.kanban.service;

import java.util.List;
import ru.kanban.model.Task;

public interface HistoryManager {
    void setToViewed(Task task);

    void addToHistory(Task task);

    void remove(int id);

    List<Task> getViewedTasks();

}
