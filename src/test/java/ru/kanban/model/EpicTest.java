package ru.kanban.model;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EpicTest {
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
                "Second subtask", "Subtask of Second Epic", Status.NEW, firstEpic
        );
    }

    @Test
    void whenAddSubtaskThenEpicSubtaskListContainsCurrentSubtask() {
        firstEpic.addSubtask(
                firstSubtask
        );
        List<Subtask> result = firstEpic.getSubtasks();
        assertThat(result).containsExactly(firstSubtask);
    }

    @Test
    void whenEpicHaveNotSubtasksThenUpdatedStatusNEW() {
        firstEpic.updateStatus();
        assertThat(firstEpic.getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    void whenEpicHasAllSubtasksWithStatusIN_PROGRESSThenUpdatedStatusIN_PROGRESS() {

        firstSubtask.setStatus(Status.IN_PROGRESS);
        secondSubtask.setStatus(Status.IN_PROGRESS);
        firstEpic.addSubtask(firstSubtask);
        firstEpic.addSubtask(secondSubtask);
        firstEpic.updateStatus();
        assertThat(firstEpic.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void whenEpicHasAllSubtasksWithStatusNEWThenUpdatedStatusNEW() {
        firstEpic.setStatus(Status.DONE);
        firstSubtask.setStatus(Status.NEW);
        secondSubtask.setStatus(Status.NEW);
        firstEpic.addSubtask(firstSubtask);
        firstEpic.addSubtask(secondSubtask);
        firstEpic.updateStatus();
        assertThat(firstEpic.getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    void whenEpicHasAllSubtasksWithStatusDONEThenUpdatedStatusDONE() {
        firstSubtask.setStatus(Status.DONE);
        secondSubtask.setStatus(Status.DONE);
        firstEpic.addSubtask(firstSubtask);
        firstEpic.addSubtask(secondSubtask);
        firstEpic.updateStatus();
        assertThat(firstEpic.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void whenEpicHasSubtasksWithDifferentStatusesThenUpdatedStatusIN_PROGRESS() {
        firstSubtask.setStatus(Status.IN_PROGRESS);
        secondSubtask.setStatus(Status.NEW);
        Subtask thirdSubtask = new Subtask("Test Subtask", "Test Subtask", Status.DONE, firstEpic);
        firstEpic.addSubtask(firstSubtask);
        firstEpic.addSubtask(secondSubtask);
        firstEpic.addSubtask(thirdSubtask);
        firstEpic.updateStatus();
        assertThat(firstEpic.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void whenEpicsHasDifferentFieldsThenFalse() {
        assertThat(firstEpic.equals(secondEpic)).isFalse();
    }

    @Test
    void whenEpicsHaveSameFieldsThenTrue() {
        Epic first = new Epic("First", "Test", Status.NEW);
        Epic second = new Epic("First", "Test", Status.NEW);
        assertThat(first.equals(second)).isTrue();
    }

    @Test
    void whenEpicsHaveDifferentIdsAndSameFieldsThenNotEquals() {
        Epic anotherEpic = new Epic("First", "First Epic", Status.NEW);
        anotherEpic.setId(3);
        assertThat(firstEpic).isNotEqualTo(anotherEpic);
    }
}