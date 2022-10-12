package de.discordbot.stefan.db;

public class SQLManager {

    private SQLManager() {
    }

    public static void onCreate() {
        LiteSQL.onUpdate(
                "CREATE TABLE IF NOT EXISTS guildinfo(guildid INTEGER NOT NULL PRIMARY KEY, guestroleid INTEGER, guestchannelid INTEGER, adminchannelid INTEGER, prefix TEXT DEFAULT '!')");
    }
}