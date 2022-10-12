package de.discordbot.stefan.db;

import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQL extends DatabaSQL {

  @Override
  public void connect() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

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
