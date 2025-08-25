package ru.kanban.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.kanban.model.Task;
import ru.kanban.utils.CustomLinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, CustomLinkedList.Node<Task>> historyMap = new HashMap<>();
    private CustomLinkedList<Task> viewedTasks = new CustomLinkedList<>();

    @Override
    public void setToViewed(Task task) {
        task.setViewed(true);
    }

    @Override
    public void addToHistory(Task task) {
        if (historyMap.containsKey(task.getId())) {
            viewedTasks.removeNode(historyMap.get(task.getId()));
        }
        CustomLinkedList.Node<Task> lastViewed = viewedTasks.linkLast(task);
        historyMap.put(task.getId(), lastViewed);
    }

    @Override
    public void remove(int id) {
        CustomLinkedList.Node<Task> removed = historyMap.remove(id);
        viewedTasks.removeNode(removed);
    }

    @Override
    public List<Task> getViewedTasks() {
        return viewedTasks.getTasks();
    }

}
