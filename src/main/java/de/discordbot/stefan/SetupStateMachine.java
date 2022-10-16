package de.discordbot.stefan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SetupStateMachine extends ListenerAdapter {

  private final long userId, channelId;
  private int state;

  private long guestRoleId, guestChannelId, adminChannelId;

  public SetupStateMachine(long userId, long channelId) {
    this.userId = userId;
    this.channelId = channelId;
    this.state = 0;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    MessageChannel channel = event.getChannel();
    if (event.getAuthor().getIdLong() != this.userId) {
      return;
    }
    if (channel.getIdLong() != this.channelId) {
      return;
    }

    Guild guild = event.getGuild();
    Message msg = event.getMessage();
    String content = msg.getContentRaw();

    if (content.startsWith(Bot.getPrefix(event.getGuild()) + "reset")) {
      this.state = 0;
      channel.sendMessage("Resetting setup...").queue();
      channel.sendMessage("Please @ the guest role...").queueAfter(2, TimeUnit.SECONDS);
      return;
    }

    switch (this.state) {
      case 0:
        String roleId = content.substring(3, content.length() - 1);
        Role guestRole = event.getGuild().getRoleById(roleId);
        if (guestRole == null) {
          channel.sendMessage("Can't find role. Please send a message with the @ of the role.")
              .queue();
        } else {
          this.guestRoleId = guestRole.getIdLong();
          channel.sendMessage(
              "Please enter the channel in which the guests start with the # notation.").queue();
          this.state = 1;
        }
        break;
      case 1:
        String channelId = content.substring(2, content.length() - 1);
        TextChannel guestChannel = event.getGuild().getTextChannelById(channelId);
        if (guestChannel == null) {
          channel.sendMessage(
                  "Can't find that channel. Please send a message with the # of the text channel.")
              .queue();
        } else {
          this.guestChannelId = guestChannel.getIdLong();
          channel.sendMessage("Please enter the channel to which I should send messages to admins.")
              .queue();
          this.state = 2;
        }
        break;
      case 2:
        String adminChannelId = content.substring(2, content.length() - 1);
        TextChannel adminChannel = event.getGuild().getTextChannelById(adminChannelId);
        if (adminChannel == null) {
          channel.sendMessage(
                  "Can't find that channel. Please send a message with the # of the text channel.")
              .queue();
        } else {
          this.adminChannelId = adminChannel.getIdLong();
          channel.sendMessage("The setup is complete!").queue();
          Bot.getJDA().removeEventListener(this);
          onSetupComplete(guild);
        }
        break;
    }
  }

  private void onSetupComplete(Guild guild) {
    ResultSet set = Bot.getDb()
        .onQuery(String.format("SELECT * FROM guildinfo WHERE guildid = %d", guild.getIdLong()));

    try {
      if (set.next()) {
        Bot.getDb().onUpdate(String.format(
            "UPDATE guildinfo SET guestroleid = %d, guestchannelid = %d, adminchannelid = %d WHERE guildid = %d",
            this.guestRoleId, this.guestChannelId, this.adminChannelId, guild.getIdLong()));
      } else {
        Bot.getDb().onUpdate(String.format(
            "INSERT INTO guildinfo(guildid, guestroleid, guestchannelid, adminchannelid) VALUES (%d, %d, %d, %d)",
            guild.getIdLong(), this.guestRoleId, this.guestChannelId, this.adminChannelId));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
