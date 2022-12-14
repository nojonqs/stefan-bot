package de.discordbot.stefan.db;

import java.net.URI;
import java.net.URISyntaxException;
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

    // String dbUrl = System.getenv("JDBC_DATABASE_URL");
    URI dbUri;
    try {
      dbUri = new URI(System.getenv("DATABASE_URL"));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

    try {
      conn = DriverManager.getConnection(dbUrl, username, password);
      statement = conn.createStatement();
      System.out.println("PostgreSQL database connected");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
