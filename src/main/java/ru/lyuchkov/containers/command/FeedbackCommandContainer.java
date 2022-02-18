package ru.lyuchkov.containers.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.PrivateChannel;
import ru.lyuchkov.factories.GuildMusicManagerFactory;
import ru.lyuchkov.handlers.Command;
import ru.lyuchkov.parse.ParseUtil;
import ru.lyuchkov.player.GuildMusicManager;
import ru.lyuchkov.utils.InputUtils;
import ru.lyuchkov.utils.OutputUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FeedbackCommandContainer implements CommandContainer {
    public synchronized static void grab(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            PrivateChannel privateChannel = member.getPrivateChannel().block();
            assert privateChannel != null;
            String title = InputUtils.getValidString(guildMusicManager.player.getPlayingTrack().getInfo().title);
            String uri = guildMusicManager.player.getPlayingTrack().getInfo().uri;
            privateChannel.createMessage("Как ты просил." + "\n" + "Название: " + title + "\n" + uri).block();
        }
    }

    public synchronized static void printHelp(MessageCreateEvent event) {
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage(OutputUtils.printCommands()).block();
    }

    public synchronized static void printAlive(MessageCreateEvent event) {
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Бот работает нормально").block();
    }

    public synchronized static void printLyrics(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        String tittle = guildMusicManager.player.getPlayingTrack().getInfo().title;
        String text = ParseUtil.getText(tittle);
        if (!text.isEmpty() && text.length() < 2000) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage(tittle + "\n" + "\n").block();
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage(text).block();
        } else {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("Не нашел текст").block();
        }
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> commands = new HashMap<>();
        commands.put("grab", FeedbackCommandContainer::grab);
        commands.put("lyrics", FeedbackCommandContainer::printLyrics);
        commands.put("alive", FeedbackCommandContainer::printAlive);
        commands.put("help", FeedbackCommandContainer::printHelp);
        return commands;
    }

}
