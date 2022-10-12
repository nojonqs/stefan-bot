package de.discordbot.stefan;

import de.discordbot.stefan.db.DatabaSQL;
import de.discordbot.stefan.db.LiteSQL;
import de.discordbot.stefan.db.PostgreSQL;
import de.discordbot.stefan.db.SQLManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {

  private static String[] args;
  public static JDA api;
  private static DatabaSQL db;

  public static void main(String[] args) {
    Bot.args = args;
    String token = getToken();

    api = JDABuilder.createDefault(token)
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS).build();

    // To set up the bot and tell it which guest role to use etc.
    api.addEventListener(new SetupCommand());

    // For given the guest role to new members
    api.addEventListener(new JoinListener());

    // To periodically check members for guests and kick them after a while
    api.addEventListener(new ReadyListener());

    db = new PostgreSQL();
    db.connect();
    SQLManager.onCreate();
  }

  public static DatabaSQL getDb() {
    return Bot.db;
  }

  private static String getToken() {
    return System.getenv("TOKEN");
  }

  public static Optional<Role> getGuestRole(Guild guild) {
    ResultSet set = db.onQuery(
        String.format("SELECT guestroleid FROM guildinfo WHERE guildid = %d", guild.getIdLong()));
    if (set == null) {
      return Optional.empty();
    }

    try {
      if (set.next()) {
        long guestroleid = set.getLong("guestroleid");
        return Optional.ofNullable(guild.getRoleById(guestroleid));
      }
    } catch (SQLException e) {
      System.out.println("ERROR: SQLException thrown in Bot.getAdminChannel()");
    }
    return Optional.empty();
  }

  public static boolean isGuildSetupComplete(Guild guild) {
    ResultSet set = db.onQuery(
        String.format("SELECT * FROM guildinfo WHERE guildid = %d", guild.getIdLong()));
    if (set == null) {
      return false;
    }

    try {
      return set.next();
    } catch (SQLException e) {
      System.out.println("ERROR: SQLException thrown in Bot.isGuildSetupComplete()");
    }
    return false;
  }

  public static TextChannel getAdminChannel(Guild guild) {
    ResultSet set = db.onQuery(
        String.format("SELECT adminchannelid FROM guildinfo WHERE guildid = %d",
            guild.getIdLong()));
    if (set == null) {
      return null;
    }

    try {
      if (set.next()) {
        long adminchannelid = set.getLong("adminchannelid");
        return guild.getTextChannelById(adminchannelid);
      }
    } catch (SQLException e) {
      System.out.println("ERROR: SQLException thrown in Bot.getAdminChannel()");
    }
    return null;
  }

  public static String getPrefix(Guild guild) {
    ResultSet set = db.onQuery(
        String.format("SELECT prefix FROM guildinfo WHERE guildid = %d", guild.getIdLong()));
    if (set == null) {
      return "!";
    }

    try {
      if (set.next()) {
        return set.getString("prefix");
      }
    } catch (SQLException e) {
      System.out.println("ERROR: SQLException thrown in Bot.getPrefix()");
    }
    return "!";
  }
}