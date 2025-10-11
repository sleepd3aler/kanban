package ru.kanban.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    private HistoryManager historyManager = Managers.getDefaultHistoryManager();
    private Task task1;
    private Task task2;
    private Epic epic1;
    private Epic epic2;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void init() throws IOException {
        File tempFile = File.createTempFile("temp", "csv");
        fileBackedTaskManager = new FileBackedTaskManager(tempFile.toPath(), historyManager);
        task1 = new Task("Task 1", "Description 1", IN_PROGRESS);
//        task2 = new Task("Task 2", "Description 2", IN_PROGRESS);
        epic1 = new Epic("Epic 1", "Description 1", NEW);
//        epic2 = new Epic("Epic 2", "Description 2", IN_PROGRESS);
        subtask1 = new Subtask("Subtask 1", "Description 1", NEW, epic1);
//        subtask2 = new Subtask("Subtask 2", "Description 2", NEW, epic2);
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
    void whenFromStringToSubtaskWithMissingEpicThenExceptionThrown() {
        String string = "1,SUBTASK,Subtask 1,NEW,Description 1,3";
        assertThatThrownBy(() -> {
            fileBackedTaskManager.fromString(string);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Disabled
    void loadFromFileTest() throws IOException {
        File testFile = File.createTempFile("test", "csv");
        List<String> strings = List.of(
                "id,type,name,status,description,epic,",
                "1,TASK,Task1,IN_PROGRESS,Description1,",
                // |Тут закомментил, протестить, словил НПЕ в методе addTask - туда проверку добавил
                "2,EPIC,Epic1,NEW,Description1,",
                "3,SUBTASK,Subtask1,NEW,Description1,2"

        );
        try (PrintWriter writer = new PrintWriter(testFile)) {
            strings.forEach(writer::println);
        }
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(testFile);
        List<Task> tasks = manager.getTasks();
        List<Epic> epics = manager.getEpics();
        List<Subtask> subtasks = manager.getSubtasks();
        assertThat(manager).isNotNull();
        assertThat(tasks).isNotEmpty();

    }
}