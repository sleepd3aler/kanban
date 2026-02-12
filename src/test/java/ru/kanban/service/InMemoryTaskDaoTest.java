package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.kanban.dao.InMemoryTaskDao;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskDaoTest {

    private InMemoryTaskDao taskManager;
    private Epic firstEpic;
    private Epic secondEpic;
    private Task firstTask;
    private Task secondTask;
    private Subtask firstSubtask;
    private Subtask secondSubtask;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskDao();
        firstEpic = new Epic("First", "First Epic", Status.NEW);
        secondEpic = new Epic("Second", "Second Epic", Status.NEW);
        firstTask = new Task("First Task", "First Task", Status.NEW);
        secondTask = new Task("Second Task", "Second Task", Status.NEW);
        firstSubtask = new Subtask("First subtask", "Subtask of First Epic", Status.NEW, firstEpic);
        secondSubtask = new Subtask("Second Subtask", "Subtask of Second Epic", Status.NEW, secondEpic);
    }

    @Test
    void whenAddTaskThenManagerTasksContainsTask() {
        taskManager.addTask(firstTask);
        assertTrue(taskManager.getTasks().contains(firstTask));
    }

    @Test
    void whenGetTaskThenReturnsExpectedTask() {
        taskManager.addTask(firstTask);
        Task expected = new Task("First Task", "First Task", Status.NEW);
        expected.setId(1);
        assertThat(taskManager.getTask(1).get()).isEqualTo(expected);
    }

    @Test
    void whenGetTaskWithInvalidIdThenResultIsEmpty() {
        taskManager.addTask(firstTask);
        Optional<Task> result = taskManager.getTask(666);
        assertThat(result).isEmpty();
    }

    @Test
    void whenGetTasksThenResultContainsAllTasks() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        List<Task> result = taskManager.getTasks();
        assertThat(result).contains(firstTask, secondTask);
    }

    @Test
    void whenDeleteTaskThenManagerDoesNotContainTask() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.deleteTask(2);
        List<Task> result = taskManager.getTasks();
        assertThat(result).doesNotContain(secondTask);
    }

    @Test
    @Disabled
    void whenDeleteTaskWithInvalidIdThenThrowsException() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskManager.deleteTask(666)
        );
        assertThat(exception.getMessage()).isEqualTo("Task with id: 666 not found");
    }

    @Test
    void whenUpdateTaskThenManagerDoesntContainsPreviousTask() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        secondTask.setId(firstTask.getId());
        taskManager.updateTask(secondTask);
        String expectedName = secondTask.getName();
        String result = taskManager.getTask(1).get().getName();
        assertThat(result).isEqualTo(expectedName);
    }

    @Test
    @Disabled
    void whenUpdateTaskWithInvalidIdThenThrowsException() {
        taskManager.addTask(firstTask);
        secondTask.setId(777);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskManager.updateTask(secondTask)
        );
        assertThat(exception.getMessage()).isEqualTo("Task with id: 777 not found");
    }

    @Test
    void whenDeleteAllTasksThenManagerTasksIsEmpty() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getTasks().isEmpty());
    }

    @Test
    void whenAddEpicThenManagerContainsEpic() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        assertThat(taskManager.getEpics()).contains(firstEpic, secondEpic);
    }

    @Test
    void whenGetEpicThenResultIsSame() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        Epic result = taskManager.getEpic(1).get();
        assertThat(result).isEqualTo(firstEpic);
    }

    @Test
    void whenGetEpicWithInvalidIdThenResultIsEmpty() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        Optional<Epic> result = taskManager.getEpic(666);
        assertThat(result).isEmpty();
    }

    @Test
    void whenGetEpicsThenManagersEpicListContainsEpics() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        List<Epic> result = taskManager.getEpics();
        assertThat(result).contains(firstEpic, secondEpic);
    }

    @Test
    void whenDeleteEpicThenManagerDoesNotContainEpic() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.deleteEpic(1);
        List<Epic> result = taskManager.getEpics();
        assertThat(result).doesNotContain(firstEpic);
    }

    @Test
    @Disabled
    void whenDeleteEpicWithInvalidIdThenThrowsException() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskManager.deleteEpic(666)
        );
        assertThat(exception.getMessage()).isEqualTo("Epic with id: 666 not found");
    }

    @Test
    void whenDeleteAllEpicsThenManagersEpicsIsEmpty() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getEpics().isEmpty());
    }

    @Test
    void whenUpdateEpicWithSameStatusesThenStatusStillSame() {
        Epic updatedEpic = new Epic("updated", "desc", Status.NEW);
        updatedEpic.setId(1);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.updateEpic(updatedEpic);
        assertThat(taskManager.getEpics().toString()).doesNotContain(firstEpic.getName());
        Status expectedStatus = Status.NEW;
        Status result = taskManager.getEpic(1).get().getStatus();
        assertThat(result).isEqualTo(expectedStatus);
    }

    @Test
    void whenUpdateEpicWithDifferentStatusThenEpicStatusUpdatedCorrectly() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        secondEpic.setId(firstEpic.getId());
        taskManager.addSubtask(firstSubtask);
        taskManager.updateEpic(secondEpic);
        Status expectedStatus = Status.NEW;
        Status result = taskManager.getEpic(1).get().getStatus();
        assertThat(result).isEqualTo(expectedStatus);
    }

    @Test
    @Disabled
    void whenUpdateEpicWithInvalidIdThenThrowsException() {
        taskManager.addEpic(firstEpic);
        firstEpic.setId(666);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskManager.updateEpic(firstEpic)
        );
        assertThat(exception.getMessage()).isEqualTo("Epic with id: 666 not found");
    }

    @Test
    void whenAddSubtaskThenManagerContainsSubtask() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        assertThat(taskManager.getSubtasks()).contains(firstSubtask, secondSubtask);
    }

    @Test
    @Disabled
    void whenAddSubtaskWithInvalidEpicThenThrowsException() {
        taskManager.addEpic(firstEpic);
        secondEpic.setId(333);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskManager.addSubtask(secondSubtask)
        );
        assertThat(exception.getMessage()).isEqualTo(
                "Epic with id: " + secondSubtask.getEpic().getId() + " not found"
        );
    }

    @Test
    @Disabled
    void whenAddSubtaskThenEpicChangeStatus() {
        taskManager.addEpic(firstEpic);
        firstEpic.setStatus(Status.DONE);
        taskManager.addSubtask(firstSubtask);
        assertThat(firstEpic.getStatus()).isEqualTo(Status.NEW);
        secondEpic.setId(1);
        taskManager.addSubtask(secondSubtask);
        firstSubtask.setStatus(Status.DONE);
        taskManager.updateSubtask(firstSubtask);
        assertThat(firstEpic.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void whenGetSubtaskThenExpectedResult() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        Optional<Subtask> result = taskManager.getSubtask(firstSubtask.getId());
        assertThat(result).isEqualTo(Optional.of(firstSubtask));
    }

    @Test
    void whenGetSubtasksThenExpectedResult() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(secondSubtask);
        List<Subtask> expected = List.of(firstSubtask, secondSubtask);
        List<Subtask> result = taskManager.getSubtasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenDeleteSubtaskThenManagerDoesntContainSubtask() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(secondSubtask);
        taskManager.deleteSubtask(firstSubtask.getId());
        List<Subtask> result = taskManager.getSubtasks();
        assertThat(result).doesNotContain(firstSubtask);
    }

    @Test
    void whenDeleteSubtaskThenEpicDoesntContainSubtask() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.deleteSubtask(firstSubtask.getId());
        List<Subtask> result = firstEpic.getSubtasks();
        assertThat(result).doesNotContain(firstSubtask);
    }

    @Test
    @Disabled
    void whenDeleteSubtaskWithInvalidIdThenThrowsException() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(secondSubtask);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskManager.deleteSubtask(1)
        );
        assertThat(exception.getMessage()).isEqualTo("Subtask with id: 1 not found");
    }

    @Test
    void whenDeleteAllSubtasksIsSuccessful() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(secondSubtask);
        taskManager.deleteAllSubtasks();
        List<Subtask> result = taskManager.getSubtasks();
        assertThat(result).isEmpty();
    }

    @Test
    @Disabled
    void whenDeleteAllSubtasksThenEpicHasNoSubtasks() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(secondSubtask);
        taskManager.deleteAllSubtasks();
        assertThat(firstEpic.getSubtasks()).isEmpty();
    }

    @Test
    @Disabled
    void whenUpdateSubtaskThenEpicStatusRenewed() {
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        Subtask updatedSubtask = new Subtask("Subtask2", "Test", Status.IN_PROGRESS, firstEpic);
        updatedSubtask.setId(taskManager.getSubtask(firstSubtask.getId()).get().getId());
        taskManager.updateSubtask(updatedSubtask);
        assertThat(taskManager.getSubtask(firstSubtask.getId()).get()).isEqualTo(updatedSubtask);
        assertThat(firstEpic.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    @Disabled
    void whenGetHistoryThenReturnExpectedHistory() {
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstSubtask);
        taskManager.getTask(firstTask.getId());
        taskManager.getTask(secondTask.getId());
        taskManager.getEpic(firstEpic.getId());
        taskManager.getSubtask(firstSubtask.getId());
        List<Task> expected = List.of(firstTask, secondTask, firstEpic, firstSubtask);
//        List<Task> result = taskManager.getHistory();
//        assertThat(result).isEqualTo(expected);
    }
}