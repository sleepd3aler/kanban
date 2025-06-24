package ru.kanban.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ru.kanban.exceptions.TaskNotFoundException;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    @Test
    void whenAddTaskThenManagerTasksContainsTask() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Task", "Test", Status.NEW);
        manager.addTask(task);
        assertTrue(manager.getTasks().contains(task));
    }

    @Test
    void whenGetTaskThenReturnsExpectedTask() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Task", "Test", Status.NEW);
        manager.addTask(task);
        Task expected = manager.getTask(1).get();
        assertThat(manager.getTask(1).get()).isEqualTo(expected);
    }

    @Test
    void whenGetTaskWithInvalidIdThenResultIsEmpty() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Task", "Test", Status.NEW);
        manager.addTask(task);
        Optional<Task> result = manager.getTask(666);
        assertThat(result).isEmpty();
    }

    @Test
    void whenGetTasksThenResultContainsAllTasks() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task", "Test", Status.NEW);
        Task task2 = new Task("Task1", "Test1", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        List<Task> result = manager.getTasks();
        assertThat(result).contains(task1, task2);
    }

    @Test
    void whenDeleteTaskThenManagerDoesNotContainTask() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task", "Test", Status.NEW);
        Task task2 = new Task("Task2", "Test2", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.deleteTask(2);
        List<Task> result = manager.getTasks();
        assertThat(result).doesNotContain(task2);
    }

    @Test
    void whenDeleteTaskWithInvalidIdThenThrowsException() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task", "Test", Status.NEW);
        Task task2 = new Task("Task2", "Test2", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> manager.deleteTask(666)
        );
        assertThat(exception.getMessage()).isEqualTo("Task with id: 666 not found");

    }

    @Test
    void whenUpdateTaskThenManagerDoesntContainsPreviousTask() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task", "Test", Status.NEW);
        Task task2 = new Task("Task2", "Test2", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        task2.setId(task1.getId());
        manager.updateTask(task2);
        assertFalse(manager.getTasks().contains(task1));
    }

    @Test
    void whenUpdateTaskWithInvalidIdThenThrowsException() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task", "Test", Status.NEW);
        Task task2 = new Task("Task2", "Test2", Status.NEW);
        manager.addTask(task1);
        task2.setId(777);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> manager.updateTask(task2)
        );
        assertThat(exception.getMessage()).isEqualTo("Task with id: 777 not found");
    }

    @Test
    void whenDeleteAllTasksThenManagerTasksIsEmpty() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task", "Test", Status.NEW);
        Task task2 = new Task("Task2", "Test2", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.deleteAllTasks();
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void whenAddEpicThenManagerContainsEpic() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        assertThat(manager.getEpics()).contains(epic1, epic2);
    }

    @Test
    void whenGetEpicThenResultIsSame() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        Epic result = manager.getEpic(1).get();
        assertThat(result).isEqualTo(epic1);
    }

    @Test
    void whenGetEpicWithInvalidIdThenResultIsEmpty() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        Optional<Epic> result = manager.getEpic(666);
        assertThat(result).isEmpty();
    }

    @Test
    void whenGetEpicsThenManagersEpicListContainsEpics() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        List<Epic> result = manager.getEpics();
        assertThat(result).contains(epic1, epic2);
    }

    @Test
    void whenDeleteEpicThenManagerDoesNotContainEpic() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.deleteEpic(1);
        List<Epic> result = manager.getEpics();
        assertThat(result).doesNotContain(epic1);
    }

    @Test
    void whenDeleteEpicWithInvalidIdThenThrowsException() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> manager.deleteEpic(666)
        );
        assertThat(exception.getMessage()).isEqualTo("Epic with id: 666 not found");
    }

    @Test
    void whenDeleteAllEpicsThenManagersEpicsIsEmpty() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.deleteAllEpics();
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void whenUpdateEpicThenEpicUpdated() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        epic2.setId(epic1.getId());
        manager.updateEpic(epic2);
        assertThat(manager.getEpics()).doesNotContain(epic1);
    }

    @Test
    void whenUpdateEpicWithInvalidIdThenThrowsException() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("Task", "Test", Status.NEW);
        Epic updatedEpic = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic);
        updatedEpic.setId(666);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> manager.updateEpic(updatedEpic)
        );
        assertThat(exception.getMessage()).isEqualTo("Epic with id: 666 not found");
    }

    @Test
    void whenAddSubtaskThenManagerContainsSubtask() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW,
                epic1);
        Subtask subtask2 = new Subtask("Subtask2", "Test", Status.NEW,
                epic2
        );
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        assertThat(manager.getSubtasks()).contains(subtask1, subtask2);
    }

    @Test
    void whenAddSubtaskWithInvalidEpicThenThrowsException() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        Epic epic2 = new Epic("Task2", "Test2", Status.NEW);
        manager.addEpic(epic1);
        epic2.setId(666);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW,
                epic2
        );
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> manager.addSubtask(subtask1)
        );
        assertThat(exception.getMessage()).isEqualTo("Epic with id: 666 not found");
    }

    @Test
    void whenAddSubtaskThenEpicChangeStatus() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("Task", "Test", Status.DONE);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask", "Test", Status.NEW, epic);
        manager.addSubtask(subtask1);
        assertThat(epic.getStatus()).isEqualTo(Status.NEW);
        Subtask subtask2 = new Subtask("Subtask2", "Test", Status.DONE, epic);
        manager.addSubtask(subtask2);
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        assertThat(epic.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void whenGetSubtaskThen() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Epic1", "Test1", Status.NEW);
        manager.addEpic(epic1);
        Subtask expected = new Subtask("Subtask1", "Test", Status.NEW,
                epic1);
        manager.addSubtask(expected);
        Optional<Subtask> result = manager.getSubtask(expected.getId());
        assertThat(result).isEqualTo(Optional.of(expected));
    }

    @Test
    void whenGetSubtasksThen() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask2);
        List<Subtask> expected = List.of(subtask1, subtask2);
        List<Subtask> result = manager.getSubtasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenDeleteSubtaskThenManagerDoesntContainSubtask() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask2);
        manager.deleteSubtask(subtask1.getId());
        List<Subtask> result = manager.getSubtasks();
        assertThat(result).doesNotContain(subtask1);
    }

    @Test
    void whenDeleteSubtaskWithInvalidIdThenThrowsException() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask2);
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> manager.deleteSubtask(1)
        );
        assertThat(exception.getMessage()).isEqualTo("Subtask with id: 1 not found");
    }

    @Test
    void whenDeleteAllSubtasksIsSuccessful() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask2);
        manager.deleteAllSubtasks();
        List<Subtask> result = manager.getSubtasks();
        assertThat(result).isEmpty();
    }

    @Test
    void whenDeleteAllSubtasksThenEpicHasNoSubtasks() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask2);
        manager.deleteAllSubtasks();
        assertThat(epic1.getSubtasks()).isEmpty();
    }

    @Test
    void whenUpdateSubtaskThenEpicStatusRenewed() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic1 = new Epic("Task", "Test", Status.NEW);
        manager.addEpic(epic1);
        Subtask first = new Subtask("Subtask1", "Test", Status.NEW, epic1);
        manager.addSubtask(first);
        Subtask updatedSubtask = new Subtask("Subtask2", "Test", Status.IN_PROGRESS, epic1);
        updatedSubtask.setId(first.getId());
        manager.updateSubtask(updatedSubtask);
        assertThat(manager.getSubtask(updatedSubtask.getId()).get()).isEqualTo(updatedSubtask);
        assertThat(epic1.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void whenGetHistoryThenReturnExpectedHistory() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task1", "Test", Status.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Task2", "Test", Status.NEW);
        manager.addTask(task2);
        Epic epic1 = new Epic("Epic", "Test", Status.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Test", Status.NEW, epic1);
        manager.addSubtask(subtask1);
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        List<Task> expected = List.of(task1, task2, epic1, subtask1);
        List<Task> result = manager.getHistory();
        assertThat(result).isEqualTo(expected);
    }
}