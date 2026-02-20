package ru.kanban.service;

import java.io.File;
import java.io.IOException;
import ru.kanban.dao.FileBackedHistoryDao;
import ru.kanban.dao.FileBackedTaskDao;
import ru.kanban.dao.TaskDao;
import ru.kanban.validator.TaskValidator;

public class FileBackedTaskServiceTest extends TaskServiceTest {

    @Override
    TaskService createService() throws IOException {
        File tempFile = File.createTempFile("Test", ".csv");
        HistoryService historyService = new HistoryServiceImpl(new FileBackedHistoryDao(tempFile.toString()));
        TaskDao taskDao = new FileBackedTaskDao(tempFile.toString());
        return new TaskServiceImpl(taskDao, historyService, new TaskValidator());
    }
}
