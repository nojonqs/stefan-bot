package de.discordbot.stefan;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Optional;

public class JoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        if (!Bot.isGuildSetupComplete(guild)) return;

        Optional<Role> guestRole = Bot.getGuestRole(guild);
        if (!guestRole.isPresent()) {
            TextChannel channel = Bot.getAdminChannel(guild);
            assert channel != null;
            channel.sendMessage("ERROR: No guest role with case-insensitive name 'guest' or 'gast' found.").queue();
            return;
        }

        Member member = event.getMember();
        guild.addRoleToMember(member, guestRole.get()).queue();
    }
}
