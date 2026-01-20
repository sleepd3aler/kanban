package ru.kanban.utils;

import java.sql.Connection;
import java.sql.DriverManager;
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
}
