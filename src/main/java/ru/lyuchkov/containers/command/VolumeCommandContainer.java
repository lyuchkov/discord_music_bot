package ru.lyuchkov.containers.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.factories.GuildMusicManagerFactory;
import ru.lyuchkov.handlers.Command;
import ru.lyuchkov.player.GuildMusicManager;
import ru.lyuchkov.utils.InputUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class VolumeCommandContainer implements CommandContainer{
    @Override
    public Map<String, Command> getCommands() {
        ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
        commands.put("volume", VolumeCommandContainer::setVolume);
        return commands;
    }
    public synchronized static void setVolume(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        final String content = InputUtils.getValidCommand("volume", event.getMessage().getContent()).replaceAll(" ", "");
        if (!content.isEmpty()) {
            try {
                int volume = Integer.parseInt(content);
                if (volume >= 200 || volume < 0) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block())
                            .createMessage("Вот скажи мне, ты идиот?").block();
                    return;
                }
                guildMusicManager.player.setVolume(volume);
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block())
                        .createMessage("Меняю.").block();
            } catch (NumberFormatException e) {
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block())
                        .createMessage("Некорректный формат команды.").block();
            }
        }
    }
}
