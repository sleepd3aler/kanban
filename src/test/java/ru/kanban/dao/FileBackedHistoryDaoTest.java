package ru.kanban.dao;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileBackedHistoryDaoTest extends HistoryDaoTest {
    private File tempFile;

    @Override
    HistoryDao createHistoryDao() throws IOException {
        tempFile = File.createTempFile("temp", ".csv");
        return new FileBackedHistoryDao(tempFile.toString());
    }

    @Test
    void whenAddTaskToHistoryThenFileContainsTask() throws IOException {
        task1.setId(1);
        epic1.setId(2);
        subtask1.setId(3);
        historyDao.addToHistory(task1);
        historyDao.addToHistory(epic1);
        historyDao.addToHistory(subtask1);
        File historyFile = ((FileBackedHistoryDao) historyDao).getHistoryFile();
        try (PrintWriter writer = new PrintWriter((tempFile.toString()))) {
            writer.println("1,TASK,Task 1,IN_PROGRESS,Description 1,");
            writer.println("2,EPIC,Epic 1,NEW,Description 1,");
            writer.println("3,SUBTASK,Subtask 1,NEW,Description 1,2");
        }
        assertThat(tempFile).hasSameTextualContentAs(historyFile);
    }

    @Test
    void whenAddToHistoryWithoutWritingToFileThenFileIsEmpty() {
        ((FileBackedHistoryDao) historyDao).addWithoutWrite(task1);
        ((FileBackedHistoryDao) historyDao).addWithoutWrite(epic1);
        File historyFile = ((FileBackedHistoryDao) historyDao).getHistoryFile();
        assertThat(historyFile).isEmpty();
    }

}

