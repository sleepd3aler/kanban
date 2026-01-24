package ru.kanban.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import ru.kanban.configutations.Config;

public class DbUtils {
    private DbUtils() {
    }

    public static Connection getConnection(Config config) throws SQLException {
        return DriverManager.getConnection(
                config.get("url"),
                config.get("username"),
                config.get("password")
        );
    }

    public static void setAutoCommit(Connection connection, boolean value) {
        if (connection != null) {
            try {
                connection.setAutoCommit(value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void commit(Connection connection) {
        if (connection != null) {
            try {
                connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addAllToHistory(Connection connection, String type) {
        try (PreparedStatement deleteStmt = connection.prepareStatement("delete from history where type = ?"

        ); PreparedStatement insertStmt = connection.prepareStatement("""
                insert into history (task_id, type)
                SELECT t.id, t.type from tasks t
                where type = ? order by id desc limit 10
                """)) {
            deleteStmt.setString(1, type);
            insertStmt.setString(1, type);
            deleteStmt.execute();
            insertStmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
