package de.discordbot.stefan.db;

import de.discordbot.stefan.Bot;

public class SQLManager {

  private SQLManager() {

  }

  public static void onCreate() {
    Bot.getDb().onUpdate(
        "CREATE TABLE IF NOT EXISTS guildinfo(guildid BIGINT NOT NULL PRIMARY KEY, guestroleid BIGINT, guestchannelid BIGINT, adminchannelid BIGINT, prefix TEXT DEFAULT '!')");
  }
}