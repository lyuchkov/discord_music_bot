package ru.lyuchkov.containers.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.factories.GuildMusicManagerFactory;
import ru.lyuchkov.handlers.Command;
import ru.lyuchkov.player.GuildMusicManager;
import ru.lyuchkov.utils.InputUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BreakCommandContainer implements CommandContainer {
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

    public synchronized static void seek(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        String content = InputUtils.getValidCommand("seek", event.getMessage().getContent())
                .replaceAll(" ", "");
        guildMusicManager.scheduler.seek(Long.parseLong(content, 10)*1000);
        Objects.requireNonNull(event.getMessage()
                .getChannel().block()).
                createMessage("Передвинул на " + content + " секунд").block();
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> commands = new HashMap<>();
        commands.put("pause", BreakCommandContainer::pause);
        commands.put("resume", BreakCommandContainer::resume);
        commands.put("seek", BreakCommandContainer::seek);
        return commands;
    }
}
