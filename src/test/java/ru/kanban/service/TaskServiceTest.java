package ru.kanban.service;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.kanban.model.Epic;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.kanban.model.Status.*;

public abstract class TaskServiceTest {
    protected TaskService taskService;
    protected Task task1;
    protected Task task2;
    protected Task task3;

    protected Epic epic1;
    protected Epic epic2;

    protected Subtask subtask1;
    protected Subtask subtask2;

    abstract TaskService createService() throws IOException;

    @BeforeEach
    void setUp() throws IOException {
        taskService = createService();
        task1 = new Task("task1", "desc", NEW);
        task2 = new Task("task2", "desc", IN_PROGRESS);
        task3 = new Task("task3", "desc", NEW);
        epic1 = new Epic("epic1", "desc", NEW);
        epic2 = new Epic("epic2", "desc", NEW);
        subtask1 = new Subtask("subtask1", "desc", NEW, epic1);
        subtask2 = new Subtask("subtask2", "desc", NEW, epic2);
    }

    @Test
    @DisplayName("Тест вставки любой задачи")
    void whenAddAnyTaskSuccessful() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        assertThat(taskService.getTasks()).hasSize(3).containsExactly(task1, task2, task3);
        assertThat(taskService.getEpics()).hasSize(2).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).hasSize(2).containsExactly(subtask1, subtask2);
    }

    @Test
    @DisplayName("Тест на выброс исключения при попытке вставить null")
    void whenTryAddNullTasksThenIllegalArgumentExceptionThrown() {
        assertThatThrownBy(() -> taskService.addTask(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TASK not null required");
        assertThatThrownBy(() -> taskService.addEpic(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EPIC not null required");
        assertThatThrownBy(() -> taskService.addSubtask(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUBTASK not null required");
    }

    @Test
    @DisplayName("Тест на выброс исключения при попытке вставить Subtask без существующего Эпика")
    void whenTryAddSubtaskAndServiceDoesntContainsEpicThenTaskNotFoundExceptionThrown() {
        assertThatThrownBy(() -> taskService.addSubtask(subtask1)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EPIC with id: " + subtask1.getEpic().getId() + " not found");
    }

    @Test
    @DisplayName("Тест на попытку получения задачи любого типа")
    void whenGetAnyTypeTaskThenExpectedResult() {
        taskService.addTask(task1);
        taskService.addEpic(epic1);
        taskService.addSubtask(subtask1);
        Task expected = task1;
        Epic expectedEpic = epic1;
        Subtask expectedSubtask = subtask1;
        assertThat(taskService.getTask(task1.getId())).isEqualTo(expected);
        assertThat(taskService.getEpic(epic1.getId())).isEqualTo(expectedEpic);
        assertThat(taskService.getSubtask(subtask1.getId())).isEqualTo(expectedSubtask);
        assertThat(taskService.getTask(task1.getId()).isViewed()).isTrue();
        assertThat(taskService.getEpic(epic1.getId()).isViewed()).isTrue();
        assertThat(taskService.getSubtask(subtask1.getId()).isViewed()).isTrue();
    }

    @Test
    @DisplayName("Тест на попадание задач в историю после их чтения и смены на \"просмотрена\"")
    void whenGetTasksThenHistoryContainsTasksAndTasksIsViewed() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Task> tasks = taskService.getTasks();
        for (Task task : tasks) {
            assertThat(task.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(task1, task2, task3)
                .doesNotContain(epic1, epic2, subtask1, subtask2);
    }

    @Test
    @DisplayName("Тест на отсутствие задачи любого типа в хранилище после удаления")
    void whenDeleteAnyTaskThenServiceDoesntContainsTask() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        taskService.deleteTask(task2.getId());
        taskService.deleteEpic(epic1.getId());
        assertThat(taskService.getTasks()).doesNotContain(task2);
        assertThat(taskService.getEpics()).doesNotContain(epic1);
        assertThat(taskService.getSubtasks()).doesNotContain(subtask1);

        taskService.deleteSubtask(subtask2.getId());
        assertThat(taskService.getSubtasks()).isEmpty();
    }

    @Test
    @DisplayName("Тест на выброс исключения при попытке удаления по невалидному ID")
    void whenDeleteAnyTaskWithIllegalIdThenTaskNotFoundExceptionThrown() {
        int id = 666;
        assertThatThrownBy(() -> taskService.deleteTask(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TASK with id: " + id + " not found");

        assertThatThrownBy(() -> taskService.deleteEpic(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EPIC with id: " + id + " not found");

        assertThatThrownBy(() -> taskService.deleteSubtask(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SUBTASK with id: " + id + " not found");
    }

    @Test
    @DisplayName("Тест на попытку обновления TASK ")
    void whenUpdateTaskThenTaskUpdated() {
        // добавили 2 задачи
        taskService.addTask(task1);
        taskService.addTask(task2);
        //таск 3 теперь с айди 2, имя  -- таск 3
        task3.setId(task2.getId());
        Task updated = taskService.updateTask(task3); // -- имя таск 3
        assertThat(taskService.getTask(task2.getId()).getName()).isEqualTo(updated.getName());
    }

    @Test
    @DisplayName("Тест на выброс исключения при попытке обновить задачу любого типа передав null")
    void whenUpdateAnyTaskWithNullThenIllegalExceptionThrown() {
        assertThatThrownBy(() -> taskService.updateTask(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TASK not null required");

        assertThatThrownBy(() -> taskService.updateEpic(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EPIC not null required");

        assertThatThrownBy(() -> taskService.updateSubtask(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUBTASK not null required");
    }

    @Test
    @DisplayName("Тест на выброс исключения при попытке обновить несуществующую задачу любого типа")
    void whenUpdateAnyTaskWithIllegalIdThenTaskNotFoundThrown() {
        assertThatThrownBy(() -> taskService.updateTask(task1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TASK with id: " + task1.getId() + " not found");

        assertThatThrownBy(() -> taskService.updateEpic(epic1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EPIC with id: " + epic1.getId() + " not found");

        assertThatThrownBy(() -> taskService.updateSubtask(subtask1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SUBTASK with id: " + subtask1.getId() + " not found");
    }

    @Test
    @DisplayName("Тест на обновление заявки помеченной как \"не просмотрена\" и отсутствие её в истории")
    void whenUpdateWithDbDaoNonViewedTaskThenHistoryDoesntContainsTask() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        task3.setId(task2.getId());
        taskService.getTask(task2.getId());
        taskService.updateTask(task3);
        assertThat(taskService.getHistory()).isEmpty();
    }

    @Test
    @DisplayName("Тест на отсутствие всех TASK в сервисе и истории после удаления всех задач этого типа")
    void whenDeleteAllTasksThenTaskServiceAndHistoryDoesntContainsTasks() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllTasks();
        assertThat(taskService.getTasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(task1, task2);

        assertThat(taskService.getEpics()).containsExactly(epic1, epic2);
        assertThat(taskService.getSubtasks()).containsExactly(subtask1, subtask2);
    }

    @Test
    @DisplayName("Тест на попадание Эпиков в историю после их чтения и смены на \"просмотрена\"")
    void whenGetEpicsWIthDbDaoThenHistoryContainsEpicsAndIsViewed() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Epic> epics = taskService.getEpics();
        for (Epic epic : epics) {
            assertThat(epic.isViewed()).isTrue();
        }
        assertThat(taskService.getHistory())
                .containsExactly(epic1, epic2)
                .doesNotContain(task1, task2, subtask1, subtask2);
    }

    @Test
    @DisplayName("Тест на отсутствие эпиков в сервисе и истории после их удаления")
    void whenDeleteAllEpicsThenTaskServiceAndHistoryDoesntContainsTasks() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllEpics();
        assertThat(taskService.getEpics()).isEmpty();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(epic1, epic2, subtask1, subtask2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    @DisplayName("Тест на обновление эпика и последующего пересчета статуса")
    void whenUpdateEpicThenEpicUpdatedAndStatusRenewed() {
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        Epic epic3 = new Epic("updated", "updated desc", DONE);
        epic3.setId(epic2.getId());
        Epic updated = taskService.updateEpic(epic3);
        assertThat(updated.getStatus()).isEqualTo(NEW);
        assertThat(taskService.getEpic(epic2.getId()).getName()).isEqualTo(updated.getName());
    }

    @Test
    @DisplayName("Тест на пересчет статуса эпика после добавления подзадачи")
    void whenAddSubtaskThenEpicStatusRenewed() {
        Epic newEpic = new Epic("new Epic", "new desc", NEW);
        Subtask newSub = new Subtask("new subtask", "any desc", IN_PROGRESS, newEpic);
        taskService.addEpic(newEpic);
        taskService.addSubtask(newSub);
        assertThat(taskService.getEpic(newEpic.getId()).getStatus()).isEqualTo(IN_PROGRESS);
    }

    @Test
    @DisplayName("Тест на попадание подзадач в историю после их чтения и смены на \"просмотрена\"")
    void whenGetSubtasksThenHistoryContainsSubtasksAndSubtaskIsViewed() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        List<Subtask> test = taskService.getSubtasks();

        for (Subtask subtask : test) {
            assertThat(subtask.isViewed()).isTrue();
        }

        assertThat(taskService.getHistory())
                .containsExactly(subtask1, subtask2)
                .doesNotContain(task1, task2, epic1, epic2);
    }

    @Test
    @DisplayName("Тест на отсутствие подзадач в сервисе и истории после их удаления")
    void whenDeleteAllSubtasksThenTaskServiceAndHistoryDoesntContainsTasks() {
        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);
        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);
        taskService.getTasks();
        taskService.getEpics();
        taskService.getSubtasks();
        taskService.deleteAllSubtasks();
        assertThat(taskService.getSubtasks()).isEmpty();
        assertThat(taskService.getHistory()).doesNotContain(subtask1, subtask2);
        assertThat(taskService.getHistory()).containsExactly(task1, task2, task3, epic1, epic2);
        assertThat(taskService.getTasks()).containsExactly(task1, task2, task3);
    }

    @Test
    @DisplayName("Тест на выброс исключения при попытке обновить подзадачу с несуществующим эпиком")
    void whenUpdateSubtaskButServiceDoesntContainsEpicThenTaskNotFoundThrown() {
        Subtask updated = new Subtask("upd", "desc", IN_PROGRESS,
                new Epic("epic", "desc", NEW));
        taskService.addEpic(epic1);
        taskService.addSubtask(subtask1);
        updated.setId(subtask1.getId());
        assertThatThrownBy(() -> taskService.updateSubtask(updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EPIC with id: " + updated.getEpic().getId() + " not found");
    }

    @Test
    @DisplayName("Тест на отсутствие подзадачи после обновления на не \"просмотренную\"")
    void whenUpdateNonViewedSubtaskThenHistoryDoesntContainsTask() {
        taskService.addEpic(epic1);
        taskService.addEpic(epic2);

        taskService.addSubtask(subtask1);
        taskService.addSubtask(subtask2);

        Epic epic3 = new Epic("new epic", "desc", NEW);
        taskService.addEpic(epic3);
        Subtask updated = new Subtask("upd", "desc", NEW, epic3);
        updated.setId(subtask2.getId());
        taskService.getSubtask(subtask2.getId());
        taskService.updateSubtask(updated);
        assertThat(taskService.getHistory()).isEmpty();
    }
}


