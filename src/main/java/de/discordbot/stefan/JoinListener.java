package de.discordbot.stefan;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Optional;

public class JoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        System.out.printf("Guild %s: User %s joined.%n", event.getGuild(), event.getMember());
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
        TextChannel channel = Bot.getAdminChannel(guild);
        assert channel != null;
        try {
            guild.addRoleToMember(member, guestRole.get()).queue();
            System.out.printf("Guild %s: gave User %s the role %s.%n", event.getGuild(), event.getMember(), guestRole.get());
        } catch (HierarchyException e) {
            channel.sendMessage(String.format("ERROR: I can't add the role %s to member %s, since the guest role is above mine.", guestRole.get(), member)).queue();
        } catch (InsufficientPermissionException e) {
            channel.sendMessage(String.format("ERROR: I can't add the role %s to member %s, due to missing permissions.", guestRole.get(), member)).queue();
        }
    }
}
