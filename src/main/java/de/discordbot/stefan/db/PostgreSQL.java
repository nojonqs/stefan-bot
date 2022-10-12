package de.discordbot.stefan.db;

import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQL extends DatabaSQL {

  @Override
  public void connect() {
    String dbUrl = System.getenv("JDBC_DATABASE_URL");
    try {
      conn = DriverManager.getConnection(dbUrl);
      statement = conn.createStatement();
      System.out.println("PostgreSQL database connected");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
