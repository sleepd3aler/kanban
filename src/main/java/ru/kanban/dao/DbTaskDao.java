package ru.kanban.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;
import ru.kanban.utils.DbUtils;

import static ru.kanban.utils.Constants.*;

public class DbTaskDao implements TaskDao, AutoCloseable {
    private Connection connection;

    public DbTaskDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Task addTask(Task task) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO  tasks (name, description, viewed,status, type) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = setStatement(statement, task);
            if (resultSet.next()) {
                task.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    @Override
    public Optional<Task> getTask(int id) {
        return getTaskByIdAndType(id, TASK_TYPE);
    }

    @Override
    public List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        try (
                PreparedStatement selectStmt = connection.prepareStatement(
                        "SELECT * from tasks where type = ?")) {
            selectStmt.setString(1, TASK_TYPE);
            ResultSet resultSet = selectStmt.executeQuery();
            while (resultSet.next()) {
                Task task = new Task(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")));
                task.setId(resultSet.getInt(1));
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
                "delete from tasks where id = ? and type = ?")) {
            Optional<Task> deleted = getTaskByIdAndType(id, TASK_TYPE);
            if (deleted.isPresent()) {
                deleteSmt.setInt(1, id);
                deleteSmt.setString(2, TASK_TYPE);
                deleteSmt.executeUpdate();
                printMsg(deleted, id);
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        try {
            return updateBy(task, TASK_TYPE) != 0 ? Optional.of(task) : Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deleteAllTasks() {
        try {
            deleteAllByType(TASK_TYPE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Epic addEpic(Epic epic) {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into  tasks(name, description, viewed, status, type) values (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = setStatement(statement, epic);
            if (resultSet.next()) {
                epic.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return epic;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        return getTaskByIdAndType(id, EPIC_TYPE);
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
                "delete from tasks where id = ? and type = ?")) {
            Optional<Epic> deleted = getTaskByIdAndType(id, EPIC_TYPE);
            if (deleted.isPresent()) {
                deleteStmt.setInt(1, id);
                deleteStmt.setString(2, EPIC_TYPE);
                deleteStmt.executeUpdate();
                printMsg(deleted, id);
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllEpics() {
        try {
            deleteAllByType(EPIC_TYPE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Epic> updateEpic(Epic epic) {
        try {
            return updateBy(epic, EPIC_TYPE) != 0 ? Optional.of(epic) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
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
            if (resultSet.next()) {
                subtask.setId(resultSet.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return subtask;
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        return getTaskByIdAndType(id, SUBTASK_TYPE);
    }

    @Override
    public List<Subtask> getSubtasks() {
        List<Subtask> result = new ArrayList<>();
        try (PreparedStatement selectStmt = connection.prepareStatement(
                """
                        select s.id, s.name, s.description, s.status, s.epic_id,
                               e.name e_name, e.description e_desc, e.status e_status
                        from tasks s
                        join tasks e  on e.id = s.epic_id;
                        """)) {
            ResultSet resultSet = selectStmt.executeQuery();
            while (resultSet.next()) {
                Epic epic = new Epic(
                        resultSet.getString("e_name"),
                        resultSet.getString("e_desc"),
                        Status.valueOf(resultSet.getString("e_status")));
                epic.setId(resultSet.getInt("epic_id"));
                Subtask subtask = new Subtask(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")), epic);
                subtask.setId(resultSet.getInt("id"));
                result.add(subtask);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteSubtask(int id) {
        try (PreparedStatement deleteStmt = connection.prepareStatement(
                "DELETE  from tasks where id = ? and type = ?")) {
            deleteStmt.setInt(1, id);
            deleteStmt.setObject(2, SUBTASK_TYPE);
            return deleteStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        try {
            deleteAllByType(SUBTASK_TYPE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Subtask> updateSubtask(Subtask subtask) {
        try {
            return updateBy(subtask, SUBTASK_TYPE) != 0 ? Optional.of(subtask) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEpicStatus(int epicId, Status status) {
        try (
                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE tasks set status = ? where id = ?")) {
            updateStatement.setString(1, status.name());
            updateStatement.setInt(2, epicId);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Status> getEpicSubtasksStatuses(int epicId) {
        List<Status> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "select status from tasks where type = ? and epic_id = ?")) {
            statement.setString(1, SUBTASK_TYPE);
            statement.setInt(2, epicId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(Status.valueOf(resultSet.getString(1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void renewAllStatuses(String type, String status) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tasks set status = ? where type = ?")) {
            statement.setObject(1, status);
            statement.setObject(2, type);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Task> Optional<T> getTaskByIdAndType(int id, String type) {

        try (PreparedStatement selectStmt = connection.prepareStatement("""
                select t.type as type,
                       t.id as id,
                       t.name as name,
                       t.description as description,
                       t.status as status,
                       e.id as e_id,
                       e.name as e_name,
                       e.description as e_desc,
                       e.status as e_status
                from tasks t
                         left join tasks e on t.epic_id = e.id
                where t.id = ? and t.type = ?
                """)) {
            selectStmt.setInt(1, id);
            selectStmt.setString(2, type);
            ResultSet resultSet = selectStmt.executeQuery();
            if (resultSet.next()) {
                switch (type) {
                    case TASK_TYPE -> {
                        Task task = new Task(
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                Status.valueOf(resultSet.getString("status")));
                        task.setId(id);
                        return Optional.ofNullable((T) task);
                    }
                    case EPIC_TYPE -> {
                        Epic epic = new Epic(
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                Status.valueOf(resultSet.getString("status")));
                        epic.setId(id);
                        return Optional.ofNullable((T) epic);
                    }

                    case SUBTASK_TYPE -> {
                        Epic epic = new Epic(
                                resultSet.getString("e_name"),
                                resultSet.getString("e_desc"),
                                Status.valueOf(resultSet.getString("e_status")));
                        epic.setId(resultSet.getInt("e_id"));
                        Subtask subtask = new Subtask(
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                Status.valueOf(resultSet.getString("status")), epic);
                        subtask.setId(id);
                        return Optional.ofNullable((T) subtask);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private void deleteAllByType(String type) throws SQLException {
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
            statement.setBoolean(4, false);
            statement.setInt(5, task.getId());
            statement.setString(6, type);
            return statement.executeUpdate();
        }
    }

    private ResultSet setStatement(PreparedStatement statement, Task task) throws SQLException {
        statement.setString(1, task.getName());
        statement.setString(2, task.getDescription());
        statement.setBoolean(3, task.isViewed());
        statement.setString(4, task.getStatus().name());
        statement.setString(5, task.getType().name());
        statement.execute();
        return statement.getGeneratedKeys();
    }

    @Override
    public boolean contains(int id, String type) {
        try (PreparedStatement statement = connection.prepareStatement(
                "select id from tasks where id = ? and type = ?")) {
            statement.setInt(1, id);
            statement.setString(2, type);
            return statement.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    @Override
    public void begin() {
        DbUtils.setAutoCommit(connection, false);
    }

    @Override
    public void rollback() {
        DbUtils.rollback(connection);
        DbUtils.setAutoCommit(connection, true);
    }

    @Override
    public void commit() {
        DbUtils.commit(connection);
        DbUtils.setAutoCommit(connection, true);
    }
}
