package ru.kanban.service;

import java.util.LinkedList;
import java.util.List;
import ru.kanban.model.Task;

import static ru.kanban.utils.Constants.FIRST_IN_HISTORY;
import static ru.kanban.utils.Constants.HISTORY_SIZE;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> viewedTasks = new LinkedList<>();

    @Override
    public void setToViewed(int id) {
        for (Task task : viewedTasks) {
            if (task.getId() == id) {
                task.setViewed(true);
            }
        }
    }

    @Override
    public void addToHistory(Task task) {
        if (viewedTasks.size() == HISTORY_SIZE) {
            viewedTasks.remove(FIRST_IN_HISTORY);
        }
        viewedTasks.add(task);
    }

    @Override
    public List<Task> getViewedTasks() {
        return new LinkedList<>(viewedTasks);
    }
}
