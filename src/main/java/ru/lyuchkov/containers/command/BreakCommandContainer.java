package ru.lyuchkov.containers.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.factories.GuildMusicManagerFactory;
import ru.lyuchkov.handlers.Command;
import ru.lyuchkov.player.GuildMusicManager;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class BreakCommandContainer implements CommandContainer{
    @Override
    public Map<String, Command> getCommands() {
        ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
        commands.put("pause", BreakCommandContainer::pause);
        commands.put("resume", BreakCommandContainer::resume);
        return commands;
    }
    public synchronized static void pause(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.setPause(true);
    }

    public synchronized static void resume(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.setPause(false);

    }
}
