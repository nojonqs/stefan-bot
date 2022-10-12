package de.discordbot.stefan;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.concurrent.Task;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReadyListener extends ListenerAdapter {
    private static final double DAYS_BEFORE_KICK = 30.0;
    @Override
    public void onReady(ReadyEvent event) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        ZonedDateTime firstCall = now.withHour(12).withMinute(0).withSecond(0);

        if (now.compareTo(firstCall) > 0) {
            firstCall = firstCall.plusDays(1);
        }

        Duration durationUntilFirstCall = Duration.between(now, firstCall);
        long initialDelayFirstCall = durationUntilFirstCall.getSeconds();

        // Check guests every 12 hours at starting at 12:00:00
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::guestUserWarningsAndKicks, initialDelayFirstCall, TimeUnit.HOURS.toSeconds(12), TimeUnit.HOURS);
    }

    public void guestUserWarningsAndKicks() {
        for (Guild guild : Bot.api.getGuilds()) {
            if (!Bot.isGuildSetupComplete(guild)) continue;

            Optional<Role> guestRole = Bot.getGuestRole(guild);
            if (!guestRole.isPresent()) continue;

            Task<List<Member>> guestMembers = guild.findMembersWithRoles(guestRole.get());
            guestMembers.onSuccess(guests -> {
                for (Member guest : guests) {
                    // Only kick members with JUST the guest role
                    if (guest.getRoles().size() > 1) continue;

                    // Calculate time until the player should be kicked
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
                    ZonedDateTime joinTime = guest.getTimeJoined().atZoneSameInstant(ZoneId.of("Europe/Berlin"));
                    long secondsSinceJoin = Duration.between(joinTime, now).getSeconds();
                    double daysSinceJoin = secondsSinceJoin / (double) (60 * 60 * 24);
                    double daysUntilKick = DAYS_BEFORE_KICK - (double) secondsSinceJoin;
                    long secondsUntilKick = (long) (daysUntilKick * (60 * 60 * 24));

                    // notify admins a specific time before the guest will be kicked
                    if (daysSinceJoin >= DAYS_BEFORE_KICK - 3 && daysSinceJoin < DAYS_BEFORE_KICK) {
                        TextChannel adminChannel = Bot.getAdminChannel(guild);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss");
                        adminChannel.sendMessage(String.format("User <@%s> will be kicked in %.2f days (at %s)", guest.getIdLong(), daysUntilKick, now.plusSeconds(secondsUntilKick).format(formatter))).queue();
                    }
                    // kick the guest if its time is up
                    else if (daysSinceJoin >= DAYS_BEFORE_KICK) {
                        TextChannel adminChannel = Bot.getAdminChannel(guild);
                        adminChannel.sendMessage(String.format("User <@%s> was kicked!", guest.getIdLong())).queue();
                        guest.kick().queue();
                    }
                }
            });


        }
    }
}
