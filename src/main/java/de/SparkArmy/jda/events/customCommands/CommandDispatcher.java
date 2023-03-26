package de.SparkArmy.jda.events.customCommands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.JdaFramework;
import de.SparkArmy.jda.events.customCommands.commands.*;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDispatcher extends ListenerAdapter {

    private final Set<CustomCommand> commands = ConcurrentHashMap.newKeySet();
    private final ConfigController controller;
    private final Logger logger;

    public CommandDispatcher(@NotNull JdaFramework jdaFramework) {
        this.controller = jdaFramework.getController();
        this.logger = jdaFramework.getLogger();
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new ArchiveSlashCommand());
        registerCommand(new UpdateSlashCommand());
        registerCommand(new BanSlashCommand());
        registerCommand(new KickSlashCommand());
        registerCommand(new MuteSlashCommand());
    }

    private void registerCommand(CustomCommand c) {
        if (commands.contains(c)) {
            logger.error("Command: " + c.getName() + " already registered");
            return;
        }
        commands.add(c);
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        for (CustomCommand c : commands) {
            if (event instanceof SlashCommandInteractionEvent && event.getName().equals(c.getName())) {
                c.dispatchSlashEvent((SlashCommandInteractionEvent) event, controller);
            } else if (event instanceof MessageContextInteractionEvent && event.getName().equals(c.getName())) {
                c.dispatchMessageContextEvent((MessageContextInteractionEvent) event, controller);
            } else if (event instanceof UserContextInteractionEvent && event.getName().equals(c.getName())) {
                c.dispatchUserContextEvent((UserContextInteractionEvent) event, controller);
            }
        }
    }
}