package ru.kanban.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import ru.kanban.configurations.Config;

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
                if (!connection.isClosed()) {
                    connection.setAutoCommit(value);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
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
}
