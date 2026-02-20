package ru.kanban.service;

import ru.kanban.dao.HistoryDao;
import ru.kanban.dao.InMemoryHistoryDao;
import ru.kanban.dao.InMemoryTaskDao;
import ru.kanban.dao.TaskDao;
import ru.kanban.validator.TaskValidator;

public class InMemoryTaskServiceTest extends TaskServiceTest {

    @Override
    TaskService createService() {
        HistoryDao historyDao = new InMemoryHistoryDao();
        TaskDao taskDao = new InMemoryTaskDao();
        HistoryService historyService = new HistoryServiceImpl(historyDao);
        return new TaskServiceImpl(taskDao, historyService, new TaskValidator());
    }

}
