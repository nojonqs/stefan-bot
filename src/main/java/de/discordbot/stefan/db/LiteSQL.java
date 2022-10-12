package de.discordbot.stefan.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LiteSQL {

    private static Connection conn;
    private static Statement statement;

    private LiteSQL() {
    }

    public static void connect() {
        conn = null;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            File file = new File("database.db");
            if (!file.exists()) {
                file.createNewFile();
            }
            String url = "jdbc:sqlite:" + file.getPath();
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();

            System.out.println("Verbindung zur Datenbank hergestellt");
        } catch (SQLException |IOException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Verbindung zur Datenbank getrennt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void onUpdate(String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet onQuery(String sql) {
        try {
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}