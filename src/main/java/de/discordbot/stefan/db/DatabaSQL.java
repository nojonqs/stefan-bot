package de.discordbot.stefan.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DatabaSQL {

  protected Connection conn;
  protected Statement statement;

  public abstract void connect();

  public void disconnect() {
    if (conn != null) {
      try {
        conn.close();
        System.out.println("PostgreSQL database connection closed");
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void onUpdate(String sql) {
    try {
      statement.execute(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ResultSet onQuery(String sql) {
    try {
      return statement.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
