package ru.lyuchkov.containers.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import ru.lyuchkov.factories.GuildMusicManagerFactory;
import ru.lyuchkov.handlers.Command;
import ru.lyuchkov.player.GuildMusicManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PassCommandContainer implements CommandContainer {

    public synchronized static void join(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) {
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) {
                    channel.join(spec -> spec.setProvider(guildMusicManager.provider)).block();
                    guildMusicManager.setConnected(true);
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Готов включать фонк").block();
                }
            }
        }
    }

    public synchronized static void exit(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) {
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) {
                    channel.sendDisconnectVoiceState().block();
                    guildMusicManager.player.stopTrack();
                    guildMusicManager.scheduler.clear();
                    guildMusicManager.setConnected(false);
                    QueueCommandContainer.clearQueue(event);
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Пока").block();
                }
            }
        }
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> commands = new HashMap<>();
        commands.put("join", PassCommandContainer::join);
        commands.put("exit", PassCommandContainer::exit);
        return commands;
    }
}
