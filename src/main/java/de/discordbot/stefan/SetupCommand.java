package de.discordbot.stefan;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SetupCommand extends ListenerAdapter {

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    if (!event.isFromGuild()) {
      return;
    }

    Member member = event.getMember();
    assert member != null;

    if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
      return;
    }

    String command = Bot.getPrefix(event.getGuild()) + "setup";
    if (event.getMessage().getContentRaw().startsWith(command)) {
      User author = event.getAuthor();
      MessageChannel channel = event.getChannel();

      channel.sendMessage("Please @ the guest role...").queue();
      Bot.api.addEventListener(new SetupStateMachine(author.getIdLong(), channel.getIdLong()));
    }
  }
}
