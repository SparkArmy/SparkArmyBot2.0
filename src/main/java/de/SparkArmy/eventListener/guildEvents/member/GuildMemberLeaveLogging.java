package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.AuditLogUtils;
import de.SparkArmy.utils.ChannelUtils;
import de.SparkArmy.utils.LogChannelType;
import de.SparkArmy.utils.punishmentUtils.PunishmentUtils;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class GuildMemberLeaveLogging extends CustomEventListener {

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild eventGuild = event.getGuild();
        User user = event.getUser();

        eventGuild.retrieveAuditLogs().queueAfter(3, TimeUnit.SECONDS,list-> {
            AuditLogEntry lastKickEntry = AuditLogUtils.getAuditLogEntryByUser(user,ActionType.KICK,list);
            AuditLogEntry lastBanEntry = AuditLogUtils.getAuditLogEntryByUser(user,ActionType.BAN,list);
                if (lastBanEntry != null && lastBanEntry.getType().equals(ActionType.BAN) && lastBanEntry.getTimeCreated().isAfter(OffsetDateTime.now().minusSeconds(4))) {
                    ChannelUtils.logInLogChannel(user.getAsTag() + " bannend", eventGuild, LogChannelType.LEAVE);
                    PunishmentUtils.sendBanOrKickEmbed(lastBanEntry,user);
                } else if (lastKickEntry != null && lastKickEntry.getType().equals(ActionType.KICK) && lastKickEntry.getTimeCreated().isAfter(OffsetDateTime.now().minusSeconds(4))) {
                    ChannelUtils.logInLogChannel(user.getAsTag() + " kicked", eventGuild, LogChannelType.LEAVE);
                    PunishmentUtils.sendBanOrKickEmbed(lastKickEntry,user);
                } else {
                    ChannelUtils.logInLogChannel(user.getAsTag() + " leaved", eventGuild, LogChannelType.LEAVE);
                }

        });
    }
}
