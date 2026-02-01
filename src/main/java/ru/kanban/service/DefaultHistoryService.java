package ru.kanban.service;

import java.util.List;
import ru.kanban.dao.HistoryDao;
import ru.kanban.model.Task;

public class DefaultHistoryService implements HistoryService {
    private HistoryDao historyDao;

    public DefaultHistoryService(HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    @Override
    public void setToViewed(Task task) {
        if (task != null) {
            historyDao.setToViewed(task);
        }
    }

    @Override
    public void addToHistory(Task task) {
        if (task != null) {
            historyDao.setToViewed(task);
            historyDao.addToHistory(task);
        }
    }

    @Override
    public void remove(int id) {
        historyDao.remove(id);
    }

    @Override
    public List<Task> getViewedTasks() {
        return historyDao.getViewedTasks();
    }

    @Override
    public void addAll(List<? extends Task> tasks) {
        historyDao.addAll(tasks);
    }

    @Override
    public void deleteAllByType(String type) {
        historyDao.deleteAllByType(type);
    }

}
