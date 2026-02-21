package ru.kanban.dao;

import java.io.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.kanban.model.Status.IN_PROGRESS;
import static ru.kanban.model.Status.NEW;

class FileBackedTaskDaoTest extends DaoTest {
    File tempFile;
    File tempHistoryFile;

    @Override
    TaskDao createDao() throws IOException {
        tempHistoryFile = File.createTempFile("temp_history", ".csv");
        tempFile = File.createTempFile("temp", ".csv");
        return new FileBackedTaskDao(tempFile.toString());
    }

    @Test
    void testTaskToString() throws IOException {
        FileBackedTaskDao fileDao = new FileBackedTaskDao(tempFile.toString());
        String expected = "0,TASK,task1,NEW,desc,";
        String result = fileDao.toString(task1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testEpicToString() throws IOException {
        FileBackedTaskDao fileDao = new FileBackedTaskDao(tempFile.toString());
        String expected = "0,EPIC,epic1,NEW,desc,";
        String result = fileDao.toString(epic1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testSubtaskToString() throws IOException {
        FileBackedTaskDao fileDao = new FileBackedTaskDao(tempFile.toString());
        String expected = "0,SUBTASK,subtask1,NEW,desc,0";
        String result = fileDao.toString(subtask1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testFromStringToTask() throws IOException {
        FileBackedTaskDao fileDao = new FileBackedTaskDao(tempFile.toString());
        String string = "1,TASK,Task 1,IN_PROGRESS,Description 1,";
        Task expected = new Task("Task 1", "Description 1", IN_PROGRESS);
        expected.setId(1);
        Task res = fileDao.fromString(string);
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void testFromStringToEpic() throws IOException {
        FileBackedTaskDao fileDao = new FileBackedTaskDao(tempFile.toString());
        String string = "1,EPIC,Epic 1,NEW,Description 1,";
        Epic expected = new Epic("Epic 1", "Description 1", NEW);
        expected.setId(1);
        Epic res = (Epic) fileDao.fromString(string);
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void testFromStringToSubtask() throws IOException {
        FileBackedTaskDao fileDao = new FileBackedTaskDao(tempFile.toString());
        fileDao.addEpic(epic1);
        String string = "1,SUBTASK,Subtask 1,NEW,Description 1,1";
        Subtask expected = new Subtask("Subtask 1", "Description 1", NEW, epic1);
        expected.setId(1);
        Subtask res = (Subtask) fileDao.fromString(string);
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void whenFromStringWithIllegalTaskThenExceptionThrown() throws IOException {
        File tempFile = File.createTempFile("temp", ".csv");
        FileBackedTaskDao fileDao = new FileBackedTaskDao(tempFile.toString());
        String string = "1,SUBTASKk,Subtask 1,NEW,Description 1,3";
        assertThatThrownBy(() ->
                fileDao.fromString(string)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal task type.");
    }

    @Test
    void whenSaveThenFileExistsByIdExpectedContent() throws IOException {
        File file = File.createTempFile("fileToCompare", "csv");
        try (PrintWriter writer = new PrintWriter((file.toString()))) {
            writer.println("id,type,name,status,description,epic");
            writer.println("1,TASK,task1,NEW,desc,");
            writer.println("2,EPIC,epic1,NEW,desc,");
            writer.println("3,SUBTASK,subtask1,NEW,desc,2");
        }
        taskDao.addTask(task1);
        taskDao.addEpic(epic1);
        taskDao.addSubtask(subtask1);
        assertThat(tempFile).hasSameTextualContentAs(file);
    }

    @Test
    void whenIncorrectPathThenExceptionThrown() throws IOException {
        tempFile = File.createTempFile("temp", "csv");
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() ->
                FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenLoadFromFileThenManagerHasSameContent() throws IOException {
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        taskDao.addTask(task1);
        taskDao.addTask(task2);
        taskDao.addTask(task3);
        taskDao.addEpic(epic1);
        taskDao.addEpic(epic2);
        taskDao.addSubtask(subtask1);
        taskDao.addSubtask(subtask2);
        List<Task> tasksBeforeLoad = taskDao.getTasks();
        List<Epic> epicsBeforeLoad = taskDao.getEpics();
        List<Subtask> subtasksBeforeLoad = taskDao.getSubtasks();
        FileBackedTaskDao newManager = FileBackedTaskDao.loadFromFile(args);
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
                FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Not enough arguments, for execute. Enter paths: to TaskManager and History");

    }

    @Test
    void whenHistoryFileIsNotCsvThenExceptionThrownWithExpectedMessage() {
        String[] args = {tempFile.toString(), ""};
        assertThatThrownBy(() ->
                FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Illegal file extension. Expected : .csv");
    }

    @Test
    void whenTaskFileIsNotCsvThenExceptionThrownWithExpectedMessage() throws IOException {
        String[] args = {"", tempHistoryFile.toString()};
        assertThatThrownBy(() ->
                FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Illegal file extension. Expected : .csv");
    }

    @Test
    void whenTaskFileNotExistsThenExceptionThrownWithExpectedMessage() throws IOException {
        String[] args = {"./example_root.csv", tempHistoryFile.toString()};
        assertThatThrownBy(() ->
                FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining(
                        "File with tasks not found");
    }

    @Test
    void whenHistoryFileNotExistsThenExceptionThrownWithExpectedMessage() {
        String[] args = {tempFile.toString(), "./example_root.csv"};
        assertThatThrownBy(() ->
                FileBackedTaskDao.loadFromFile(args))
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
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Must be: id,type,name,status,description,epic id");
    }

    @Test
    void whenTaskFileExistsByIdWrongIdThenExceptionThrownWithCorrectMessage() throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic
                    asd,TASK,Task,NEW,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task ID is missing.");
    }

    @Test
    void whenTaskFileExistsByIdIllegalTypeThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic
                    1,Type,Task,NEW,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal task type.");
    }

    @Test
    void whenTaskFileExistsByIdIllegalNameThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic
                    1,TASK, ,NEW,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task name is missing");
    }

    @Test
    void whenTaskFileExistsByIdIllegalStatusThenExceptionThrownWithCorrectMessage() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile))
        )) {
            writer.println("""
                    id,type,name,status,description,epic
                    1,TASK,Task,STATUS,description,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
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
                    id,type,name,status,description,epic
                    1,TASK,Task,NEW, ,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
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
                    id,type,name,status,description,epic
                    1,SUBTASK,Subtask,NEW,desc, ,
                    """);
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic ID at subtask: Subtask is invalid");
    }

    @Test
    void whenHistoryCsvExistsByIdAnotherContentThenExceptionThrownWithCorrectMessage() throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("HelloWorld");
        }
        taskDao.addTask(task1);
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Must be: id,type,name,status,description,epic ");
    }

    @Test
    void whenHistoryExistsByIdIllegalIDThenExceptionThrownWithCorrectMessage() throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("asd,Type,Name,NEW,Desc");
        }
        taskDao.addTask(task1);
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task ID is missing.");
    }

    @Test
    void whenHistoryExistsByIdIllegalTypeThenExceptionThrownWithCorrectMessage() throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,Type,Name,New,Desc");
        }
        taskDao.addTask(task1);
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal task type.");
    }

    @Test
    void whenHistoryExistsByIdIllegalNameThenExceptionThrownWithCorrectMessage() throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,TASK, ,NEW,Desc");
        }
        taskDao.addTask(task1);
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task name is missing");
    }

    @Test
    void whenHistoryExistsByIdIllegalDescThenExceptionThrownWithCorrectMessage() throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,TASK,Task,NEW, ,");
        }
        taskDao.addTask(task1);
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description is missing");
    }

    @Test
    void whenHistoryExistsByIdSubtaskWithEmptyEpicIDThenExceptionThrownWithCorrectMessage() throws IOException {
        taskDao.addTask(task1);
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,SUBTASK,Subtask,NEW,Desc, ");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic ID at subtask: Subtask is invalid");
    }

    @Test
    void whenHistoryExistsByIdSubtaskWithoutEpicThenExceptionThrownWithCorrectMessage() throws IOException {
        taskDao.addTask(task1);
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tempHistoryFile))
        )) {
            writer.println("1,SUBTASK,Subtask,NEW,Desc,");
        }
        String[] args = {tempFile.toString(), tempHistoryFile.toString()};
        assertThatThrownBy(() -> FileBackedTaskDao.loadFromFile(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Epic ID at subtask: Subtask is missing");
    }
}