package ru.kanban.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    private Task firstTask;
    private Task secondTask;

    @BeforeEach
    void setUp() {
        this.firstTask = new Task("First Task", "First Task", Status.NEW);
        this.secondTask = new Task("Second Task", "Second Task", Status.NEW);
    }

@Test
    void whenTasksHasSameFieldsThenTrue() {
    Task another = new Task("First Task", "First Task", Status.NEW);
    assertThat(firstTask).isEqualTo(another);
}

@Test
    void whenTasksHasDifferentFieldsThenFalse() {
    assertThat(firstTask).isNotEqualTo(secondTask);
}

@Test
    void whenTasksHasDifferentIdsAndSameFieldsThenNotEquals() {
    secondTask.setId(333);
    assertThat(firstTask).isNotEqualTo(secondTask);
}

}