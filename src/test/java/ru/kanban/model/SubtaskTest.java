package ru.kanban.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubtaskTest {
    private Epic firstEpic;
    private Epic secondEpic;
    private Subtask firstSubtask;
    private Subtask secondSubtask;

    @BeforeEach
    void setUp() {
        firstEpic = new Epic("First", "First Epic", Status.NEW);
        secondEpic = new Epic("Second", "Second Epic", Status.NEW);
        firstSubtask = new Subtask(
                "First subtask", "Subtask of First Epic", Status.NEW, firstEpic
        );
        secondSubtask = new Subtask(
                "Second Subtask", "Subtask of Second Epic", Status.NEW, secondEpic
        );
    }

    @Test
    void whenSubtaskHasDifferentEpicsAndSameFieldsThenNotEquals() {
        Subtask anotherSubtask = new Subtask(
                "First subtask", "Subtask of First Epic", Status.NEW, secondEpic
        );
        assertThat(firstSubtask).isNotEqualTo(anotherSubtask);
    }

    @Test
    void whenSubtaskHasSameEpicsButDifferentFieldsThenNotEquals() {
        Subtask anotherSubtask = new Subtask("Second", "Test", Status.NEW, firstEpic);
        assertThat(firstSubtask).isNotEqualTo(anotherSubtask);
    }

    @Test
    void whenSubtaskHasDifferentIdsAndSameFieldsThenNotEquals() {
        Subtask anotherSubtask = new Subtask(
                "First subtask", "Subtask of First Epic", Status.NEW, firstEpic
        );
        firstSubtask.setId(3);
        assertThat(firstSubtask).isNotEqualTo(secondSubtask);
    }

    @Test
    void whenSubtaskHasSameFieldsThenEquals() {
        Subtask anotherSubtask = new Subtask(
                "First subtask", "Subtask of First Epic", Status.NEW, firstEpic
        );
        assertThat(firstSubtask).isEqualTo(anotherSubtask);
    }
}