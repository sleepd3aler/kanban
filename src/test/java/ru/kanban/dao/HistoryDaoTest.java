package ru.kanban.dao;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.kanban.model.Status.IN_PROGRESS;
import static ru.kanban.model.Status.NEW;

public abstract class HistoryDaoTest {
    protected HistoryDao historyDao;
    protected Task task1;
    protected Task task2;
    protected Task task3;
    protected Epic epic1;
    protected Epic epic2;
    protected Subtask subtask1;
    protected Subtask subtask2;

    abstract HistoryDao createHistoryDao() throws IOException;

    @BeforeEach
    void setUp() throws IOException {
        historyDao = createHistoryDao();
        task1 = new Task("task1", "desc", NEW);
        task2 = new Task("task2", "desc", IN_PROGRESS);
        task3 = new Task("task3", "desc", IN_PROGRESS);
        epic1 = new Epic("epic1", "desc", NEW);
        epic2 = new Epic("epic2", "desc", NEW);
        subtask1 = new Subtask("subtask1", "desc", NEW, epic1);
        subtask2 = new Subtask("subtask2", "desc", NEW, epic2);
    }

    @Test
    void whenAddTaskHistoryContainsTask() {
        historyDao.addToHistory(task1);
        assertThat(historyDao.getViewedTasks()).containsOnly(task1);
    }

    @Test
    void whenAddEpicThenHistoryContainsEpic() {
        historyDao.addToHistory(epic1);
        assertThat(historyDao.getViewedTasks()).containsOnly(epic1);
    }

    @Test
    void whenAddSubtaskThenHistorySubtask() {
        historyDao.addToHistory(subtask1);
        assertThat(historyDao.getViewedTasks()).containsOnly(subtask1);
    }

    @Test
    void whenAddAllTypesOfTaskThenHistoryContainsAll() {
        task1.setId(1);
        epic1.setId(2);
        subtask1.setId(3);
        historyDao.addToHistory(task1);
        historyDao.addToHistory(epic1);
        historyDao.addToHistory(subtask1);
        assertThat(historyDao.getViewedTasks()).containsExactly(task1, epic1, subtask1);
    }

    @Test
    void whenAddMoreThan10TasksThenHistoryDoesntContainsFirstViewedWillAbsent() {
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
        historyDao.addToHistory(task1);
        historyDao.addToHistory(task2);
        historyDao.addToHistory(task3);
        for (int i = 4; i <= 11; i++) {
            Task lastTask = new Task("task" + i, "desc", NEW);
            lastTask.setId(i);
            historyDao.addToHistory(lastTask);
        }
        assertThat(historyDao.getViewedTasks()).hasSize(10)
                .doesNotContain(task1)
                .contains(task2, task3);
    }

    @Test
    void whenSetToViewedThenTaskUpdated() {
        historyDao.addToHistory(task1);
        historyDao.setToViewed(task1);
        assertThat(historyDao.getViewedTasks().get(0).isViewed()).isTrue();
    }

    @Test
    void whenRemoveThenHistoryDoesntContainsTask() {
        historyDao.addToHistory(task1);
        historyDao.remove(task1.getId());
        assertThat(historyDao.getViewedTasks()).doesNotContain(task1);
    }
}
