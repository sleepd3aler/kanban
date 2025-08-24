package ru.kanban.service;

import java.util.List;
import ru.kanban.model.Task;
import ru.kanban.utils.CustomLinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private CustomLinkedList<Task> viewedTasks = new CustomLinkedList<>();

    @Override
    public void setToViewed(Task task) {
        task.setViewed(true);
    }

    @Override
    public void addToHistory(Task task) {
        viewedTasks.add(task);
        setToViewed(task);
    }

    @Override
    public void remove(int id) {
        viewedTasks.removeNode(viewedTasks.findById(id));
    }

    @Override
    public List<Task> getViewedTasks() {
        return viewedTasks.getTasks();
    }
}
