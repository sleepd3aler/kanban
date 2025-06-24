package ru.kanban.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

@Test
    void whenTasksHasSameFieldsThenTrue() {
    Task task1 = new Task("First", "Test", Status.NEW);
    Task task2 = new Task("First", "Test", Status.NEW);
    assertThat(task1).isEqualTo(task2);
}

@Test
    void whenTasksHasDifferentFieldsThenFalse() {
    Task task1 = new Task("First", "Test", Status.NEW);
    Task task2 = new Task("Second", "Test", Status.NEW);
    assertThat(task1).isNotEqualTo(task2);
}

@Test
    void whenTasksHasDifferentIdsAndSameFieldsThenNotEquals() {
    Task task1 = new Task("First", "Test", Status.NEW);
    Task task2 = new Task("First", "Test", Status.NEW);
    task2.setId(3);
    assertThat(task1).isNotEqualTo(task2);

}

}