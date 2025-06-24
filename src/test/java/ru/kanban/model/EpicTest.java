package ru.kanban.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EpicTest {

    @Test
    void whenAddSubtaskThenEpicSubtaskListContainsCurrentSubtask() {
        Epic epic = new Epic("Epic", "Test", Status.NEW);
        epic.addSubtask(
                new Subtask("Subtask", "Subtask of epic", Status.NEW, epic)
        );

    }

    @Test
    void whenEpicHaveNotSubtasksThenUpdatedStatusNEW() {
        Epic epic = new Epic("Epic", "Test", Status.IN_PROGRESS);
        epic.updateStatus();
        assertThat(epic.getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    void whenEpicHasAllSubtasksWithStatusIN_PROGRESSThenUpdatedStatusIN_PROGRESS() {
        Epic epic = new Epic("Epic", "Test", Status.NEW);
        epic.addSubtask(new Subtask("Subtask", "Subtask of epic", Status.IN_PROGRESS, epic));
        epic.addSubtask(new Subtask("Subtask1", "Subtask of epic", Status.IN_PROGRESS, epic));
        epic.updateStatus();
        assertThat(epic.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void whenEpicHasAllSubtasksWithStatusNEWThenUpdatedStatusNEW() {
        Epic epic = new Epic("Epic", "Test", Status.IN_PROGRESS);
        epic.addSubtask(new Subtask("Subtask", "Subtask of epic", Status.NEW, epic));
        epic.addSubtask(new Subtask("Subtask2", "Subtask of epic", Status.NEW, epic));
        epic.updateStatus();
        assertThat(epic.getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    void whenEpicHasAllSubtasksWithStatusDONEThenUpdatedStatusDONE() {
        Epic epic = new Epic("Epic", "Test", Status.DONE);
        epic.addSubtask(new Subtask("Subtask", "Subtask of epic", Status.DONE, epic));
        epic.addSubtask(new Subtask("Subtask2", "Subtask of epic", Status.DONE, epic));
        epic.updateStatus();
        assertThat(epic.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void whenEpicHasSubtasksWithDifferentStatusesThenUpdatedStatusIN_PROGRESS() {
        Epic epic = new Epic("Epic", "Test", Status.NEW);
        epic.addSubtask(new Subtask("Subtask", "Subtask of epic", Status.DONE, epic));
        epic.addSubtask(new Subtask("Subtask", "Subtask of epic", Status.IN_PROGRESS, epic));
        epic.addSubtask(new Subtask("Subtask", "Subtask of epic", Status.NEW, epic));
        epic.updateStatus();
        assertThat(epic.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void whenEpicsHasDifferentFieldsThenFalse() {
        Epic first = new Epic("First", "Test", Status.NEW);
        Epic second = new Epic("Second", "Test", Status.NEW);
        assertThat(first.equals(second)).isFalse();
    }

    @Test
    void whenEpicsHaveSameFieldsThenTrue() {
        Epic first = new Epic("First", "Test", Status.NEW);
        Epic second = new Epic("First", "Test", Status.NEW);
        assertThat(first.equals(second)).isTrue();
    }

    @Test
    void whenEpicsHaveDifferentIdsAndSameFieldsThenNotEquals() {
        Epic first = new Epic("First", "Test", Status.NEW);
        Epic second = new Epic("First", "Test", Status.NEW);
        second.setId(3);
        assertThat(first).isNotEqualTo(second);
    }
}