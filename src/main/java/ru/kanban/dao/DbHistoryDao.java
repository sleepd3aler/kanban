package ru.kanban.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static ru.kanban.utils.Constants.*;
import static ru.kanban.utils.DbUtils.*;

public class DbHistoryDao implements HistoryDao, AutoCloseable {
    private Connection connection;

    public DbHistoryDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void setToViewed(Task task) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tasks set viewed = ? where id = ?")) {
            statement.setBoolean(1, true);
            statement.setInt(2, task.getId());
            task.setViewed(true);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addToHistory(Task task) {
        try (PreparedStatement updateStmt = connection.prepareStatement(
                "update history set viewed_at = current_timestamp where task_id = ?");
             PreparedStatement InsertStmt = connection.prepareStatement(
                     "INSERT INTO history (task_id, type) values (?, ?);");
             PreparedStatement deleteStmt = connection.prepareStatement(
                     """
                             DELETE FROM history
                             WHERE task_id
                             NOT IN
                                   (SELECT task_id
                                    FROM history
                                    ORDER BY viewed_at
                                    DESC  LIMIT  10)""")) {
            updateStmt.setInt(1, task.getId());
            if ((updateStmt.executeUpdate() == 0)) {
                InsertStmt.setInt(1, task.getId());
                InsertStmt.setString(2, task.getType().name());
                InsertStmt.execute();
                deleteStmt.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(int id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE  from history where task_id = ?")) {
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Task> getViewedTasks() {
        List<Task> result = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                     SELECT t.id, t.name, t.description, t.status, t.type, t.epic_id,
                     ep.name as epic_name,
                     ep.description as epic_description,
                     ep.status as epic_status
                     from tasks t
                     join history h on t.id = h.task_id
                     left join tasks ep on t.epic_id = ep.id
                    order by h.viewed_at
                    """);
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                result.add(generateByType(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Task generateByType(ResultSet resultSet) throws SQLException {
        String actual = resultSet.getString("type");
        switch (actual) {
            case TASK_TYPE -> {
                Task current = new Task(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")));
                current.setId(resultSet.getInt("id"));
                current.setViewed(true);
                return current;
            }
            case EPIC_TYPE -> {
                Epic current = new Epic(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")));
                current.setId(resultSet.getInt("id"));
                current.setViewed(true);
                return current;
            }
            case SUBTASK_TYPE -> {
                Epic epicOfSubtask = new Epic(
                        resultSet.getString("epic_name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("epic_status")));
                epicOfSubtask.setId(resultSet.getInt("epic_id"));
                Subtask current = new Subtask(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")),
                        epicOfSubtask);
                current.setId(resultSet.getInt("id"));
                current.setViewed(true);
                return current;
            }
        }
        return null;
    }

    @Override
    public void addAll(List<? extends Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        setAutoCommit(connection, false);
        try (
                PreparedStatement deleteStmt = connection.prepareStatement(
                "delete from history where type = ?"

        );
             PreparedStatement insertStmt = connection.prepareStatement("""
                     insert into history (task_id, type)
                     SELECT t.id, t.type from tasks t
                     where type = ? order by id desc limit 10
                     """);
             PreparedStatement updateStmt = connection.prepareStatement(
                     "update tasks set viewed = TRUE where type = ?");) {

            String type = tasks.get(0).getType().name();
            deleteStmt.setString(1, type);
            insertStmt.setString(1, type);
            updateStmt.setString(1, type);
            deleteStmt.execute();
            updateStmt.execute();
            insertStmt.execute();
            tasks.forEach(task -> task.setViewed(true));
            commit(connection);
        } catch (SQLException e) {
            rollback(connection);
            throw new RuntimeException(e);
        } finally {
            setAutoCommit(connection, true);
        }
    }

    @Override
    public void deleteAllByType(String type) {
        try (PreparedStatement deleteStmt = connection.prepareStatement(
                "delete from history where type = ?")) {
            deleteStmt.setString(1, type);
            deleteStmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
