package ru.kanban.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.kanban.model.Status.IN_PROGRESS;
import static ru.kanban.model.Status.NEW;

class FileBackedHistoryManagerTest {
    private FileBackedTaskManager fileBackedTaskManager;
    private File tempFile;
    private FileBackedHistoryManager historyManager;
    private Task task1;
    private Epic epic1;
    private Subtask subtask1;
    private String[] args;

    @BeforeEach
    void init() throws IOException {
        tempFile = File.createTempFile("temp", ".csv");
        File tempHistory = File.createTempFile("temp_history", ".csv");
        historyManager = Managers.getDefaultFileBackedHistoryManager(tempHistory.toString());
        fileBackedTaskManager = new FileBackedTaskManager(tempFile.toString(), historyManager);
        task1 = new Task("Task 1", "Description 1", IN_PROGRESS);
        epic1 = new Epic("Epic 1", "Description 1", NEW);
        subtask1 = new Subtask("Subtask 1", "Description 1", NEW, epic1);
        args = new String[]{tempFile.toString(), tempHistory.toString()};
    }

    @Test
    void whenAddTaskToHistoryThenFileContainsTask() throws IOException {
        File history = historyManager.getHistoryFile();
        epic1.setId(2);
        subtask1.setId(3);
        historyManager.addToHistory(task1);
        historyManager.addToHistory(epic1);
        historyManager.addToHistory(subtask1);
        try (PrintWriter writer = new PrintWriter((tempFile.toString()))) {
            writer.println("1,TASK,Task 1,IN_PROGRESS,Description 1,");
            writer.println("2,EPIC,Epic 1,NEW,Description 1,");
            writer.println("3,SUBTASK,Subtask 1,NEW,Description 1,2");
        }
        assertThat(tempFile).hasSameTextualContentAs(history);
    }

    @Test
    void whenAddToHistoryWithoutWritingToFileThenFileIsEmpty() {
        historyManager = new FileBackedHistoryManager(tempFile.toString());
        historyManager.addWithoutWrite(task1);
        historyManager.addWithoutWrite(epic1);
        assertThat(historyManager.getHistoryFile()).isEmpty();
    }

    @Test
    void whenLoadFromFileThenHistoryExpected() {
        fileBackedTaskManager.addTask(task1);
        fileBackedTaskManager.addEpic(epic1);
        fileBackedTaskManager.addSubtask(subtask1);
        fileBackedTaskManager.getTask(1);
        fileBackedTaskManager.getEpic(2);
        fileBackedTaskManager.getSubtask(3);
        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(args);
        List<Task> res = newManager.getHistory();
        assertThat(res).hasSize(3);
    }
}

