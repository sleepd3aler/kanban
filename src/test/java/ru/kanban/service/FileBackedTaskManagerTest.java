package ru.kanban.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.kanban.model.Status.IN_PROGRESS;
import static ru.kanban.model.Status.NEW;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager fileBackedTaskManager;
    private File tempFile;
    private HistoryManager historyManager;
    private Task task1;
    private Epic epic1;
    private Subtask subtask1;

    @BeforeEach
    void init() throws IOException {
        tempFile = File.createTempFile("temp", "csv");
        historyManager = Managers.getDefaultFileBackedHistoryManager();
        fileBackedTaskManager = new FileBackedTaskManager(tempFile.toPath(), historyManager);
        task1 = new Task("Task 1", "Description 1", IN_PROGRESS);
        epic1 = new Epic("Epic 1", "Description 1", NEW);
        subtask1 = new Subtask("Subtask 1", "Description 1", NEW, epic1);
        fileBackedTaskManager.addTask(task1);
        fileBackedTaskManager.addEpic(epic1);
        fileBackedTaskManager.addSubtask(subtask1);
    }

    @Test
    void testTaskToString() {
        String expected = "1,TASK,Task 1,IN_PROGRESS,Description 1,";
        String result = fileBackedTaskManager.toString(task1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testEpicToString() {
        String expected = "2,EPIC,Epic 1,NEW,Description 1,";
        String result = fileBackedTaskManager.toString(epic1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testSubtaskToString() {
        String expected = "3,SUBTASK,Subtask 1,NEW,Description 1,2";
        String result = fileBackedTaskManager.toString(subtask1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testFromStringToTask() {
        String string = "1,TASK,Task 1,IN_PROGRESS,Description 1,";
        Task expected = new Task("Task 1", "Description 1", IN_PROGRESS);
        expected.setId(1);
        Task res = fileBackedTaskManager.fromString(string);
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void testFromStringToEpic() {
        String string = "1,EPIC,Epic 1,NEW,Description 1,";
        Epic expected = new Epic("Epic 1", "Description 1", NEW);
        Epic res = (Epic) fileBackedTaskManager.fromString(string);
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void testFromStringToSubtask() {
        String string = "1,SUBTASK,Subtask 1,NEW,Description 1,2";
        Subtask expected = new Subtask("Subtask 1", "Description 1", NEW, epic1);
        Subtask res = (Subtask) fileBackedTaskManager.fromString(string);
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void whenSaveThenFileContainsExpectedContent() throws IOException {
        File file = File.createTempFile("fileToCompare", "csv");
        try (PrintWriter writer = new PrintWriter((file.toString()))) {
            writer.println("id,type,name,status,description,epic");
            writer.println("1,TASK,Task 1,IN_PROGRESS,Description 1,");
            writer.println("2,EPIC,Epic 1,NEW,Description 1,");
            writer.println("3,SUBTASK,Subtask 1,NEW,Description 1,2");
        }

        assertThat(tempFile).hasSameTextualContentAs(file);
    }

    @Test
    void whenFromStringToSubtaskWithMissingEpicThenExceptionThrown() {
        String string = "1,SUBTASK,Subtask 1,NEW,Description 1,3";
        assertThatThrownBy(() ->
                fileBackedTaskManager.fromString(string)
        ).isInstanceOf(IllegalArgumentException.class);
    }

//    @Test
//    void whenWriteToFileThenLoadFromFileSuccessful() throws IOException {
//        fileBackedTaskManager.getTask(1);
//        fileBackedTaskManager.getEpic(2);
//        fileBackedTaskManager.getSubtask(3);
//        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);
//        Task task = manager.getTask(1).get();
//        Epic epic = manager.getEpic(2).get();
//        Subtask subtask = manager.getSubtask(3).get();
//        assertThat(task).isNotNull().isEqualTo(task1);
//        assertThat(epic).isNotNull();
//        assertThat(epic.getName()).isEqualTo(epic1.getName());
//        assertThat(subtask).isNotNull();
//        assertThat(subtask.getName()).isEqualTo(subtask1.getName());
//    }

}
