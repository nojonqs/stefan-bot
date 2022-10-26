package de.discordbot.stefan;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.concurrent.Task;

public class ReadyListener extends ListenerAdapter {

  private static final double DAYS_BEFORE_KICK = 30.0;

  @Override
  public void onReady(ReadyEvent event) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
    ZonedDateTime midday = now.withHour(12).withMinute(0).withSecond(0);
    ZonedDateTime midnight = now.withHour(0).withMinute(0).withSecond(0);

    if (now.compareTo(midday) > 0) {
      midday = midday.plusDays(1);
    }
    if (now.compareTo(midnight) > 0) {
      midnight = midnight.plusDays(1);
    }

    Duration durationUntilMidday = Duration.between(now, midday);
    Duration durationUntilMidnight = Duration.between(now, midnight);
    long secondsUntilMidday = durationUntilMidday.getSeconds();
    long secondsUntilMidnight = durationUntilMidnight.getSeconds();
    long initialDelayFirstCall = Math.min(secondsUntilMidday, secondsUntilMidnight);

    // Check guests every 12 hours at starting at 12:00:00
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(this::guestUserWarningsAndKicks, initialDelayFirstCall,
        TimeUnit.HOURS.toSeconds(12), TimeUnit.SECONDS);
  }

  private long daysToSeconds(double days) {
    return (long) (days * (60.0 * 60 * 24));
  }

  private double secondsToDays(long seconds) {
    return seconds / (60.0 * 60 * 24);
  }

  public void guestUserWarningsAndKicks() {
    for (Guild guild : Bot.getJDA().getGuilds()) {
      System.out.printf("Periodic call on guild %s...", guild.toString());
      if (!Bot.isGuildSetupComplete(guild)) {
        continue;
      }
      System.out.println("guild is setup correctly!");

      Optional<Role> guestRole = Bot.getGuestRole(guild);
      if (!guestRole.isPresent()) {
        continue;
      }

      Task<List<Member>> guestMembers = guild.findMembersWithRoles(guestRole.get());
      guestMembers.onSuccess(guests -> {
        for (Member guest : guests) {
          StringBuilder sb = new StringBuilder();
          sb.append(String.format("Checking member %s in guild %s...", guest.toString(), guild));

          // Only kick members with JUST the guest role
          if (guest.getRoles().size() > 1) {
            sb.append(" has more roles, skipping!\n");
            System.out.println(sb);
            continue;
          }

          sb.append(" member is guest...");

          // Calculate time until the player should be kicked
          ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
          ZonedDateTime joinTime = guest.getTimeJoined()
              .atZoneSameInstant(ZoneId.of("Europe/Berlin"));
          long secondsSinceJoin = Duration.between(joinTime, now).getSeconds();
          double daysSinceJoin = secondsToDays(secondsSinceJoin);
          double daysUntilKick = DAYS_BEFORE_KICK - daysSinceJoin;
          long secondsUntilKick = daysToSeconds(daysUntilKick);

          // round the kick time to 12:00:00 or 00:00:00, depending on which is earlier
          ZonedDateTime kickDateTime = now.plusSeconds(secondsUntilKick);
          ZonedDateTime middayKickDateTime = kickDateTime.withHour(12).withMinute(0).withSecond(0);
          ZonedDateTime midnightKickDateTime = kickDateTime.withHour(0).withMinute(0).withSecond(0);

          if (kickDateTime.compareTo(middayKickDateTime) > 0) {
            middayKickDateTime = middayKickDateTime.plusDays(1);
          }
          if (kickDateTime.compareTo(midnightKickDateTime) > 0) {
            midnightKickDateTime = midnightKickDateTime.plusDays(1);
          }

          ZonedDateTime actualKickDateTime = middayKickDateTime.compareTo(midnightKickDateTime) < 0 ? middayKickDateTime : midnightKickDateTime;
          long secondsUntilActualKick = Duration.between(now, actualKickDateTime).getSeconds();
          double daysUntilActualKick = secondsToDays(secondsUntilActualKick);

          sb.append(String.format(" joined %.2f days ago and will be kicked in %.2f days...",
              daysSinceJoin, daysUntilActualKick));

          // notify admins a specific time before the guest will be kicked
          if (0 <= daysUntilActualKick && daysUntilActualKick <= 3) {
            sb.append(String.format(" sending warning since member will be kicked in %f days!",
                    daysUntilActualKick));
            System.out.println(sb);

            TextChannel adminChannel = Bot.getAdminChannel(guild);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss");
            adminChannel.sendMessage(
                String.format("User <@%s> will be kicked in %.2f days (at %s)", guest.getIdLong(),
                        daysUntilActualKick, now.plusSeconds(secondsUntilActualKick).format(formatter))).queue();
          }
          // kick the guest if its time is up
          // we use the not-rounded dasUntilKick, since the daysUntilActualKick will be 0 on exactly the time we do this
          // check, which may be off by a second.
          else if (daysUntilKick <= 0) {
            sb.append(String.format(" kicking member since his kick is due since %f days!", -daysUntilKick));
            System.out.println(sb);

            TextChannel adminChannel = Bot.getAdminChannel(guild);
            adminChannel.sendMessage(String.format("User <@%s> was kicked!", guest.getIdLong()))
                .queue();
            guest.kick().queue();
          }
        }
      });
    }
  }
}
