package ru.kanban.service;

import java.io.*;
import java.util.List;
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
    private File tempHistoryFile;
    private Task task1;
    private Epic epic1;
    private Subtask subtask1;

    @BeforeEach
    void init() throws IOException {
        tempFile = File.createTempFile("temp", ".csv");
        tempHistoryFile = File.createTempFile("temp_history", ".csv");
        HistoryManager historyManager = Managers.getDefaultFileBackedHistoryManager(tempHistoryFile.toString());
        fileBackedTaskManager = new FileBackedTaskManager(tempFile.toString(), historyManager);
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
        expected.setId(1);
        Epic res = (Epic) fileBackedTaskManager.fromString(string);
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void testFromStringToSubtask() {
        String string = "1,SUBTASK,Subtask 1,NEW,Description 1,2";
        Subtask expected = new Subtask("Subtask 1", "Description 1", NEW, epic1);
        expected.setId(1);
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

    @Test
    void whenIncorrectPathThenExceptionThrown() throws IOException {
        tempFile = File.createTempFile("temp", "csv");
        File tempHistoryFile = File.createTempFile("temp_history", "csv");
        HistoryManager historyManager = Managers.getDefaultFileBackedHistoryManager(tempHistoryFile.toString());
        fileBackedTaskManager = new FileBackedTaskManager(tempFile.toString(), historyManager);
        task1 = new Task("Task 1", "Description 1", IN_PROGRESS);
        epic1 = new Epic("Epic 1", "Description 1", NEW);
        subtask1 = new Subtask("Subtask 1", "Description 1", NEW, epic1);
        fileBackedTaskManager.addTask(task1);
        fileBackedTaskManager.addEpic(epic1);
        fileBackedTaskManager.addSubtask(subtask1);
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() ->
                FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenThenLoadFromFileThenManagerHasSameContent() throws FileNotFoundException {
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        List<Task> tasksBeforeLoad = fileBackedTaskManager.getTasks();
        List<Epic> epicsBeforeLoad = fileBackedTaskManager.getEpics();
        List<Subtask> subtasksBeforeLoad = fileBackedTaskManager.getSubtasks();
        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(args);
        List<Task> tasksAfterLoad = newManager.getTasks();
        List<Epic> epicsAfterLoad = newManager.getEpics();
        List<Subtask> subtasksAfterLoad = newManager.getSubtasks();
        assertThat(tasksAfterLoad).containsAll(tasksBeforeLoad);
        assertThat(epicsAfterLoad).containsAll(epicsBeforeLoad);
        assertThat(subtasksAfterLoad).containsAll(subtasksBeforeLoad);
    }

    @Test
    void whenNotEnoughArgumentsThenExceptionThrown() {
        String[] args = {tempFile.toString()};
        assertThatThrownBy(() ->
                FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Not enough arguments, for execute. Enter paths: to TaskManager and History");

    }

    @Test
    void whenHistoryFileIsNotCsvThenExceptionThrownWithExpectedMessage() {
        String[] args = {tempFile.toString(), ""};
        assertThatThrownBy(() ->
                FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Illegal file extension. Expected : .csv");
    }

    @Test
    void whenTaskFileIsNotCsvThenExceptionThrownWithExpectedMessage() {
        String[] args = {"", tempHistoryFile.toString()};
        assertThatThrownBy(() ->
                FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Illegal file extension. Expected : .csv");
    }

    @Test
    void whenTaskFileNotExistsThenExceptionThrownWithExpectedMessage() {
        String[] args = {"./example_root.csv", tempHistoryFile.toString()};
        assertThatThrownBy(() ->
                FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining(
                        "File with tasks not found");
    }

    @Test
    void whenHistoryFileNotExistsThenExceptionThrownWithExpectedMessage() {
        String[] args = {tempFile.toString(), "./example_root.csv"};
        assertThatThrownBy(() ->
                FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining(
                        "File with history not found");
    }

    @Test
    void whenTaskFileCsvStructureIsWrongThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic id
                    asd,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Must be: id,type,name,status,description,epic id");
    }

    @Test
    void whenTaskFileContainsWrongIdThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic id
                    asd,TASK,Task,NEW,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task ID is missing.");
    }

    @Test
    void whenTaskFileContainsIllegalTypeThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic id
                    1,Type,Task,NEW,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal task type.");
    }

    @Test
    void whenTaskFileContainsIllegalNameThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic id
                    1,TASK, ,NEW,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task name is missing");
    }

    @Test
    void whenTaskFileContainsIllegalStatusThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic id
                    1,TASK,Task,STATUS,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal status provided");
    }

    @Test
    void whenDescriptionIsMissingInTaskFileThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                     id,type,name,status,description,epic id
                     1,TASK,Task,NEW, ,
                     """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description is missing");
    }

    @Test
    void whenSubtaskIsMissingEpicIdAtTaskFileThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                     id,type,name,status,description,epic id
                     1,SUBTASK,Subtask,NEW,desc, ,
                     """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic ID at subtask: Subtask is invalid");
    }

    @Test
    void whenHistoryCsvContainsAnotherContentThenExceptionThrownWithCorrectMessage() throws IOException {
        tempHistoryFile = File.createTempFile("history_test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("HelloWorld");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Must be: id,type,name,status,description,epic ");
    }

    @Test
    void whenHistoryContainsIllegalIDThenExceptionThrownWithCorrectMessage() throws IOException {
        tempHistoryFile = File.createTempFile("history_test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("asd,Type,Name,NEW,Desc");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task ID is missing.");
    }

    @Test
    void whenHistoryContainsIllegalTypeThenExceptionThrownWithCorrectMessage() throws IOException {
        tempHistoryFile = File.createTempFile("history_test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,Type,Name,New,Desc");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal task type.");
    }

    @Test
    void whenHistoryContainsIllegalNameThenExceptionThrownWithCorrectMessage() throws IOException {
        tempHistoryFile = File.createTempFile("history_test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,TASK, ,NEW,Desc");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task name is missing");
    }

    @Test
    void whenHistoryContainsIllegalDescThenExceptionThrownWithCorrectMessage() throws IOException {
        tempHistoryFile = File.createTempFile("history_test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,TASK,Task,NEW, ,");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description is missing");
    }

    @Test
    void whenHistoryContainsSubtaskWithEmptyEpicIDThenExceptionThrownWithCorrectMessage() throws IOException {
        tempHistoryFile = File.createTempFile("history_test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,SUBTASK,Subtask,NEW,Desc, ");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic ID at subtask: Subtask is invalid");
    }

    @Test
    void whenHistoryContainsSubtaskWithoutEpicThenExceptionThrownWithCorrectMessage() throws IOException {
        tempHistoryFile = File.createTempFile("history_test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,SUBTASK,Subtask,NEW,Desc,");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskManager.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic ID at subtask: Subtask is missing");
    }
}
