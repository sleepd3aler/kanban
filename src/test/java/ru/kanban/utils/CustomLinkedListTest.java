package ru.kanban.utils;

import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;

class CustomLinkedListTest {

    private CustomLinkedList<Task> customLinkedList;
    private Task firstTask;
    private Task secondTask;
    private Epic firstEpic;

    @BeforeEach
    void init() {
        customLinkedList = new CustomLinkedList<>();
        firstTask = new Task("First Task", "First Task", Status.NEW);
        secondTask = new Task("Second Task", "Second Task", Status.NEW);
        firstEpic = new Epic("First", "First Epic", Status.NEW);
        customLinkedList.linkLast(firstTask);
        customLinkedList.linkLast(secondTask);
        customLinkedList.linkLast(firstEpic);
    }

    @Test
    void whenLinkLastSuccessful() {
        assertThat(customLinkedList).hasSize(3);
    }

    @Test
    void whenGetTasksThenExpectedResult() {
        List<Task> expected = List.of(
                firstTask, secondTask, firstEpic
        );
        List<Task> result = customLinkedList.getTasks();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void checkIterator() {
        Iterator<Task> iterator = customLinkedList.iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(firstTask);
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(secondTask);
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(firstEpic);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void whenAddTasksThenMapContains() {
        CustomLinkedList<Task> viewedTasks = new CustomLinkedList<>();
        viewedTasks.add(firstTask);
        secondTask.setId(2);
        viewedTasks.add(secondTask);
        firstEpic.setId(3);
        viewedTasks.add(firstEpic);
        List<Task> expected = viewedTasks.getTasks();
        assertThat(expected).hasSize(3)
                .contains(firstTask, secondTask, firstEpic);
    }

    @Test
    void whenAddViewedTaskThenMapDoesntContainsTask() {
        CustomLinkedList<Task> viewedTasks = new CustomLinkedList<>();
        viewedTasks.add(firstTask);
        secondTask.setId(2);
        viewedTasks.add(secondTask);
        viewedTasks.add(firstTask);
        assertThat(viewedTasks).hasSize(2);
    }
}