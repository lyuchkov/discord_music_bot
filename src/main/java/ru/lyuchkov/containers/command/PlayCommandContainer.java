package ru.lyuchkov.containers.command;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.factories.GuildMusicManagerFactory;
import ru.lyuchkov.handlers.Command;
import ru.lyuchkov.parse.ParseUtil;
import ru.lyuchkov.player.GuildMusicManager;
import ru.lyuchkov.utils.InputUtils;
import ru.lyuchkov.utils.UrlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayCommandContainer implements CommandContainer {
    public synchronized static void play(MessageCreateEvent event) {
        start(event, "p", false);
    }
    public synchronized static void playTop(MessageCreateEvent event) {
        start(event, "pt", true);

    }
    public static synchronized void playNow(MessageCreateEvent event) {
        start(event, "now", true);
        QueueCommandContainer.skip(event);
    }
    private static synchronized void start(MessageCreateEvent event, String commandName, boolean isTop){
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) PassCommandContainer.join(event);
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        String content = InputUtils.getValidCommand(commandName, event.getMessage().getContent());
        List<String> command = InputUtils.getValidList(content);
        AudioLoadResultHandler audioLoadResultHandler;
        if(isTop){
            audioLoadResultHandler = new AudioLoadResultHandler() {


                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    guildMusicManager.scheduler.setFirstAtQueue(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    guildMusicManager.scheduler.setFirstAtQueueList(audioPlaylist.getTracks());
                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException e) {
                    e.printStackTrace();
                }
            };
        } else {
            audioLoadResultHandler = guildMusicManager.resultHandler;
        }
        if (command.isEmpty()) return;
        try{
            if (UrlUtils.isUrl(command.get(0))) {
                String url = command.get(0);
                guildMusicManager.playerManager.loadItemOrdered(guildMusicManager, url, audioLoadResultHandler);
                if(isTop) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Добавил в начало").block();
                } else {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Добавил").block();
                }
            } else {
                String url = ParseUtil.getYoutubeUrl(command);
                if (!url.equals("error")) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Нашел: " + url).block();
                    guildMusicManager.playerManager.loadItemOrdered(guildMusicManager, url, audioLoadResultHandler);
                } else {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Не нашел.").block();
                }
            }
        }catch (FriendlyException e) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block()).
                    createMessage("Проблема с доступом в джойказино.").block();
        }
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> commands = new HashMap<>();
        commands.put("pt", PlayCommandContainer::playTop);
        commands.put("p", PlayCommandContainer::play);
        commands.put("now", PlayCommandContainer::playNow);
        return commands;
    }
}
