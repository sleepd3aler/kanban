package ru.kanban.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ru.kanban.model.Epic;
import ru.kanban.model.Status;
import ru.kanban.model.Subtask;
import ru.kanban.model.Task;

import static ru.kanban.utils.Constants.*;

public class DbHistoryManager implements HistoryManager, AutoCloseable {
    private Connection connection;

    private int counter = 0;

    public DbHistoryManager(Connection connection) {
        this.connection = connection;
        initCounter();
    }

    @Override
    public void setToViewed(Task task) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tasks set viewed = ? where id = ?"
        )) {
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
                "update history set viewed_at = current_timestamp where task_id = ?"
        ); PreparedStatement InsertStmt = connection.prepareStatement(
                "INSERT INTO history (task_id, type) values (?, ?);"
        )) {
            updateStmt.setInt(1, task.getId());
            if ((updateStmt.executeUpdate() == 0)) {
                counter++;
                InsertStmt.setInt(1, task.getId());
                InsertStmt.setString(2, task.getType().name());
                InsertStmt.execute();
                removeLastIfLimitReached();

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(int id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE  from history where task_id = ?"
        )) {
            statement.setInt(1, id);
            statement.execute();
            counter--;
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
                        Status.valueOf(resultSet.getString("status"))
                );
                current.setId(resultSet.getInt("id"));
                current.setViewed(true);
                return current;
            }
            case EPIC_TYPE -> {
                Epic current = new Epic(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status"))
                );
                current.setId(resultSet.getInt("id"));
                current.setViewed(true);
                return current;
            }
            case SUBTASK_TYPE -> {
                Epic epicOfSubtask = new Epic(
                        resultSet.getString("epic_name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("epic_status"))
                );
                epicOfSubtask.setId(resultSet.getInt("epic_id"));
                Subtask current = new Subtask(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        Status.valueOf(resultSet.getString("status")),
                        epicOfSubtask
                );
                current.setId(resultSet.getInt("id"));
                current.setViewed(true);
                return current;
            }
        }
        return null;
    }

    private void removeLastIfLimitReached() throws SQLException {
        if (counter > MAX_SIZE) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        delete
                        from history
                        where viewed_at = (select min(viewed_at) from history)
                        """);
            }
            counter--;
        }
    }

    private void initCounter() {
        this.counter = 0;
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select count(task_id) from  history");
            if (resultSet.next()) {
                counter = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
