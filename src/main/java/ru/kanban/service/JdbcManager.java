package ru.kanban.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static ru.kanban.model.Status.*;
import static ru.kanban.utils.Constants.*;

public class JdbcManager implements TaskManager {
    private Connection connection;

    private HistoryManager historyManager;

    public JdbcManager(Connection connection, HistoryManager historyManager) {
        this.connection = connection;
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getViewedTasks();
    }

    @Override
    public void addTask(Task task) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO  tasks (name, description, viewed,status, type) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = setStatement(statement, task);
            setId(resultSet, task);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Task> getTask(int id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM TASKS WHERE id = ? and type = ?")) {
            Optional<Task> task = getTaskByIdAndType(statement, id, TASK_TYPE);
            task.ifPresent(value -> {
                historyManager.setToViewed(value);
                historyManager.addToHistory(value);
            });
            return task;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * from tasks where type = ?")) {
            statement.setString(1, TASK_TYPE);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Task task = new Task(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")));
                task.setId(resultSet.getInt(1));
                task.setViewed(true);
                historyManager.setToViewed(task);
                historyManager.addToHistory(task);
                result.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Optional<Task> deleteTask(int id) {
        try (PreparedStatement deleteSmt = connection.prepareStatement(
                "delete from tasks where id = ? and type = ?");
             PreparedStatement selectStatement = connection.prepareStatement(
                     "select * from tasks where id = ? and type = ?"
             )) {

            Optional<Task> deleted = getTaskByIdAndType(selectStatement, id, TASK_TYPE);
            deleteSmt.setInt(1, id);
            deleteSmt.setString(2, TASK_TYPE);
            deleteSmt.executeUpdate();
            printMsg(deleted, id);
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        try {
            if (!task.isViewed()) {
                historyManager.remove(task.getId());
            }
            return updateBy(task, TASK_TYPE) != 0 ? Optional.of(task) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deleteAllTasks() {
        try {
            deleteAllBy(TASK_TYPE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into  tasks(name, description, viewed, status, type) values (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = setStatement(statement, epic);
            setId(resultSet, epic);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from tasks where id = ? and type = ?")) {
            Optional<Epic> result = getTaskByIdAndType(statement, id, EPIC_TYPE);
            result.ifPresent(value -> {
                historyManager.setToViewed(value);
                historyManager.addToHistory(value);
            });
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Epic> getEpics() {
        List<Epic> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from tasks where type = ?")) {
            statement.setObject(1, EPIC_TYPE);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Epic epic = new Epic(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")));
                epic.setId(resultSet.getInt("id"));
                epic.setViewed(true);
                historyManager.setToViewed(epic);
                historyManager.addToHistory(epic);
                result.add(epic);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Epic> deleteEpic(int id) {
        try (PreparedStatement deleteStmt = connection.prepareStatement(
                "delete from tasks where id = ? and type = ?");
             PreparedStatement selectStmt = connection.prepareStatement(
                     "select * from tasks where id = ? and type = ?"
             )) {
            Optional<Epic> deleted = getTaskByIdAndType(selectStmt, id, EPIC_TYPE);
            deleteStmt.setInt(1, id);
            deleteStmt.setString(2, EPIC_TYPE);
            deleteStmt.executeUpdate();
            printMsg(deleted, id);
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllEpics() {
        try {
            deleteAllBy(EPIC_TYPE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        try {
            if (!epic.isViewed()) {
                historyManager.remove(epic.getId());
            }
            return updateBy(epic, EPIC_TYPE) != 0 ? Optional.of(epic) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addSubtask(Subtask subtask) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO tasks (name, description, viewed, status, type, epic_id) values (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subtask.getName());
            statement.setString(2, subtask.getDescription());
            statement.setBoolean(3, subtask.isViewed());
            statement.setObject(4, subtask.getStatus().name());
            statement.setObject(5, subtask.getType().name());
            statement.setInt(6, subtask.getEpic().getId());
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            setId(resultSet, subtask);
            updateEpicStatus(subtask.getEpic().getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * from tasks where id = ? and type = ?")) {
            Optional<Subtask> res = getTaskByIdAndType(statement, id, SUBTASK_TYPE);
            res.ifPresent(value -> {
                historyManager.setToViewed(value);
                historyManager.addToHistory(value);
            });
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Subtask> getSubtasks() {
        List<Subtask> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from tasks where type = ?")) {
            statement.setObject(1, SUBTASK_TYPE);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Epic epic = getEpic(resultSet.getInt("epic_id")).get();
                Subtask subtask = new Subtask(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")),
                        epic);
                subtask.setId(resultSet.getInt("id"));
                subtask.setViewed(true);
                historyManager.setToViewed(subtask);
                historyManager.addToHistory(subtask);
                result.add(subtask);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Subtask> deleteSubtask(int id) {
        try (PreparedStatement deleteStmt = connection.prepareStatement(
                "DELETE  from tasks where id = ? and type = ?");
             PreparedStatement selectStmt = connection.prepareStatement(
                     "select * from tasks where id = ? and type = ?"
             )) {
            deleteStmt.setInt(1, id);
            deleteStmt.setObject(2, SUBTASK_TYPE);
            Optional<Subtask> deleted = getTaskByIdAndType(selectStmt, id, SUBTASK_TYPE);
            if (deleted.isPresent()) {
                updateEpicStatus(deleted.get().getEpic().getId());
                deleteStmt.execute();
                printMsg(deleted, id);
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deleteAllSubtasks() {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tasks set status = ? where type = ?")) {
            deleteAllBy(SUBTASK_TYPE);
            statement.setObject(1, NEW.name());
            statement.setObject(2, EPIC_TYPE);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        try {
            if (!subtask.isViewed()) {
                historyManager.remove(subtask.getId());
            }
            updateBy(subtask, SUBTASK_TYPE);
            updateEpicStatus(subtask.getEpic().getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private void updateEpicStatus(int epicId) throws SQLException {
        try (PreparedStatement selectStatement = connection.prepareStatement(
                "select status from tasks where epic_id = ?");
             PreparedStatement updateStatement = connection.prepareStatement(
                     "UPDATE tasks set status = ? where id = ?")) {
            selectStatement.setInt(1, epicId);
            updateStatement.setInt(2, epicId);
            ResultSet statuses = selectStatement.executeQuery();
            Status actual = checkSubtaskStatus(statuses);
            if (actual == NEW) {
                updateStatement.setObject(1, NEW.name());
                updateStatement.execute();
            }
            if (actual == DONE) {
                updateStatement.setObject(1, DONE.name());
                updateStatement.execute();
            }

            if (actual == IN_PROGRESS) {
                updateStatement.setObject(1, IN_PROGRESS.name());
                updateStatement.execute();
            }
        }
    }

    private Status checkSubtaskStatus(ResultSet resultSet) throws SQLException {
        boolean allNew = true;
        boolean allDone = true;
        Status status = null;
        while (resultSet.next()) {
            status = Status.valueOf(resultSet.getString("status"));

            if (!status.equals(NEW)) {
                allNew = false;
            }
            if (!status.equals(DONE)) {
                allDone = false;
            }
        }
        if (status == null || allNew) {
            return NEW;
        }
        if (allDone) {
            return DONE;
        }
        return IN_PROGRESS;
    }

    private <T extends Task> Optional<T> getTaskByIdAndType(PreparedStatement statement, int id, String type)
            throws SQLException {
        statement.setInt(1, id);
        statement.setString(2, type);
        ResultSet resultSet = statement.executeQuery();
        Task res = null;
        switch (type) {
            case TASK_TYPE -> {
                if (resultSet != null && resultSet.next()) {
                    res = new Task(
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            Status.valueOf(resultSet.getString("status")));
                    res.setId(id);
                    res.setViewed(true);
                }
                return Optional.ofNullable((T) res);
            }
            case EPIC_TYPE -> {
                if (resultSet.next()) {
                    res = new Epic(
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            Status.valueOf(resultSet.getString("status")));
                    res.setId(id);
                    res.setViewed(true);
                }
                return Optional.ofNullable((T) res);
            }
            case SUBTASK_TYPE -> {
                if (resultSet.next()) {
                    Optional<Epic> epic = findEpicById(resultSet.getInt("epic_id"));
                    if (epic.isPresent()) {
                        res = new Subtask(
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                Status.valueOf(resultSet.getString("status")),
                                epic.get());
                        res.setId(id);
                        res.setViewed(true);
                    }
                    return Optional.ofNullable((T) res);
                }
            }
        }
        return Optional.empty();
    }

    private void deleteAllBy(String type) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "delete from tasks where type = ?")) {
            statement.setString(1, type);
            statement.execute();
        }
    }

    private <T extends Task> void printMsg(Optional<T> task, int id) {
        if (task.isEmpty()) {
            System.out.println("Task with id: " + id + " has wrong type");
        }
    }

    private int updateBy(Task task, String type) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tasks set name = ?, description = ?, status = ?, viewed = ? where id = ? and type = ?")) {
            statement.setString(1, task.getName());
            statement.setString(2, task.getDescription());
            statement.setObject(3, task.getStatus().name());
            statement.setBoolean(4, task.isViewed());
            statement.setInt(5, task.getId());
            statement.setString(6, type);
            return statement.executeUpdate();
        }
    }

    private Optional<Epic> findEpicById(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("" +
                "select * from tasks where id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            Epic result = null;
            if (resultSet.next()) {
                result = new Epic(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")));
                result.setId(id);
            }
            return Optional.ofNullable(result);
        }
    }

    // private void updateToViewed(int id) throws SQLException {
    // PreparedStatement statement = connection.prepareStatement(
    // "UPDATE tasks set viewed = ? where id = ?");
    // statement.setBoolean(1, true);
    // statement.setInt(2, id);
    // statement.execute();
    // }

    private static void setId(ResultSet resultSet, Task task) throws SQLException {
        while (resultSet.next()) {
            task.setId(resultSet.getInt(1));
        }
    }

    private static ResultSet setStatement(PreparedStatement statement, Task task) throws SQLException {
        statement.setString(1, task.getName());
        statement.setString(2, task.getDescription());
        statement.setBoolean(3, task.isViewed());
        statement.setString(4, task.getStatus().name());
        statement.setString(5, task.getType().name());
        statement.execute();
        return statement.getGeneratedKeys();
    }

//    private boolean removeIfViewedUpdated(Task task) throws SQLException {
//        try (PreparedStatement statement = connection.prepareStatement(
//                "delete from history where task_id = ? and viewed_at = ?"
//        )) {
//            statement.setInt(1, task.getId());
//            statement.setBoolean(2, false);
//            statement.execute();
//            return true;
//        }
//    }

    public static void main(String[] args) throws SQLException {
        Config config = new Config();
        config.load("/db/liquibase.properties");
        Connection connection = DriverManager.getConnection(
                config.get("url"),
                config.get("username"),
                config.get("password"));
        HistoryManager historyManager = new JdbcHistoryManager(connection);
        TaskManager manager = new JdbcManager(connection, historyManager);
        Task task1 = new Task("task1", "desc", NEW);
        Task task2 = new Task("task2", "desc", NEW);
        Task task3 = new Task("task3", "desc", NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        for (Task task : manager.getTasks()) {
            System.out.println("Task is viewed: " + task.isViewed());
            historyManager.addToHistory(task);
        }
        List<Task> viewed = historyManager.getViewedTasks();
        for (Task task : historyManager.getViewedTasks()) {
            System.out.println("Task is in history: " + task);
        }
        // System.out.println("Task is viewed: " +
        // manager.getTask(task3.getId()).get().isViewed());

    }
}
