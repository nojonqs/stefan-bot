package de.discordbot.stefan.db;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LiteSQL extends DatabaSQL {

  @Override
  public void connect() {
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
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
  }
}