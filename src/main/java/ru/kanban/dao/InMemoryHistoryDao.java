package ru.kanban.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.kanban.model.Task;
import ru.kanban.utils.CustomLinkedList;
import ru.kanban.utils.Node;

public class InMemoryHistoryDao implements HistoryDao {
    private final Map<Integer, Node<Task>> historyMap = new HashMap<>();
    private final CustomLinkedList<Task> viewedTasks = new CustomLinkedList<>();

    @Override
    public void setToViewed(Task task) {
        task.setViewed(true);
    }

    @Override
    public void addToHistory(Task task) {
        if (historyMap.containsKey(task.getId())) {
            viewedTasks.removeNode(historyMap.get(task.getId()));
        }
        Node<Task> lastViewed = viewedTasks.linkLast(task);
        historyMap.put(task.getId(), lastViewed);
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            viewedTasks.removeNode(historyMap.remove(id));
        }
    }

    @Override
    public List<Task> getViewedTasks() {
        return viewedTasks.getTasks();
    }

    @Override
    public void addAll(List<? extends Task> tasks) {
        for (Task task : tasks) {
            task.setViewed(true);
            addToHistory(task);
        }
    }

    @Override
    public void deleteAllByType(String type) {
        List<Task> test = getViewedTasks();
        test.stream().filter(value -> value.getType().name().equals(type))
                .map(Task::getId)
                .forEach(this::remove);
    }
}
