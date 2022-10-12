package de.discordbot.stefan.db;

import de.discordbot.stefan.Bot;

public class SQLManager {

  private SQLManager() {
  }

  public static void onCreate() {
    Bot.getDb().onUpdate(
        "CREATE TABLE IF NOT EXISTS guildinfo(guildid INTEGER NOT NULL PRIMARY KEY, guestroleid INTEGER, guestchannelid INTEGER, adminchannelid INTEGER, prefix TEXT DEFAULT '!')");
  }
}