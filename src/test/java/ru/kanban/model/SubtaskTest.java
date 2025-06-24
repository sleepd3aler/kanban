package ru.kanban.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubtaskTest {
    @Test
    void whenSubtaskHasDifferentEpicsAndSameFieldsThenNotEquals() {
        Epic first = new Epic("First", "Test", Status.NEW);
        Epic second = new Epic("Second", "Test", Status.NEW);
        Subtask firstSubtask = new Subtask("First", "Test", Status.NEW, first);
        Subtask secondSubtask = new Subtask("First", "Test", Status.NEW, second);
        assertThat(firstSubtask).isNotEqualTo(secondSubtask);
    }

    @Test
    void whenSubtaskHasSameEpicsButDifferentFieldsThenNotEquals() {
        Epic first = new Epic("First", "Test", Status.NEW);
        Subtask firstSubtask = new Subtask("First", "Test", Status.NEW, first);
        Subtask secondSubtask = new Subtask("Second", "Test", Status.NEW, first);
        assertThat(firstSubtask).isNotEqualTo(secondSubtask);
    }

}