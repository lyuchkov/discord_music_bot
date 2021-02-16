package ru.lyuchkov;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import ru.lyuchkov.interfaces.Command;
import ru.lyuchkov.parse.Parser;
import ru.lyuchkov.player.GuildMusicManager;
import ru.lyuchkov.player.TrackScheduler;
import ru.lyuchkov.utils.ContentController;
import ru.lyuchkov.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Bot {
    private static final ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
    private static AudioPlayerManager playerManager;
    @SuppressWarnings("FieldMayBeFinal")
    private static Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();
        assert client != null;
        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);

        putCommands();
        client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .subscribe(event -> {
                    final String content = event.getMessage().getContent();
                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        if (content.startsWith('*' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });
        client.onDisconnect().block();
    }

    private static synchronized GuildMusicManager getGuildPlayerManager(Guild guild) {
        long guildId = guild.getId().asLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }
        return musicManager;
    }

    private static void putCommands() {
        commands.put("p", event -> {
            if (!isConnectedGuildManager(event))
                join(event);
            if(checkUrl(event)) {
                play(event);
            }
            else {
                String url = getUrl(event);
                if(!url.equals("null")) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Нашел: " + url).block();
                    playAndFind(event, url);
                }else {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Не нашел.").block();
                }
            }
        });
        commands.put("pt", event -> {
            if (!isConnectedGuildManager(event))
                join(event);
            if(checkUrl(event)) {
                playTop(event);
            }
            else {
                String url =getUrl(event);
                if(!url.equals("null")) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Нашел: " + url).block();
                    playAndFindTop(event, url);
                }else {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Не нашел.").block();
                }
            }
        });
        commands.put("pause", event -> {
            if (!isConnectedGuildManager(event))
                return;
            pause(event);
        });
        commands.put("resume", event -> {
            if (!isConnectedGuildManager(event))
                return;
            resume(event);
        });
        commands.put("fs", event -> {
            if (!isConnectedGuildManager(event))
                return;
            skip(event);
        });
        commands.put("j", event -> {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block()).
                    createMessage("Я здесь").block();
            join(event);
        });
        commands.put("e", event -> {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block()).
                    createMessage("Пока").block();
            clearQueue(event);
            exit(event);
        });
        commands.put("q", event -> {
            if (!isConnectedGuildManager(event))
                return;
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage(StringUtils.printQueue(getQueue(event))).block();
        });
        commands.put("h", event -> {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage(StringUtils.printCommands()).block();
        });
        commands.put("clr", event -> {
            if (!isConnectedGuildManager(event))
                return;
            Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Теперь очередь пуста.").block();
            clearQueue(event);
        });
        commands.put("vol", event -> {
            if (!isConnectedGuildManager(event))
                return;
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("Сейчас поменяю.").block();
            setVolume(event);
        });
        commands.put("rm", event -> {
            if (!isConnectedGuildManager(event))
                return;
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("Удаляю.").block();
            deleteElement(event);
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage(StringUtils.printQueue(getQueue(event))).block();
        });

    }

    private synchronized static void playAndFindTop(MessageCreateEvent event, String url) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            private final TrackScheduler scheduler = guildMusicManager.scheduler;
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                scheduler.setFirstAtQueue(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                scheduler.setFirstAtQueueList(audioPlaylist.getTracks());
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {
                e.printStackTrace();
            }
        });
    }

    private static boolean isNull(String query){
        return query == null;
    }
    private synchronized static void play(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" "));
        String url =command.get(1);
        if(!isNull(url)) {
            playerManager.loadItemOrdered(guildMusicManager, url, guildMusicManager.resultHandler);
        }
    }
    private synchronized static void playTop(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" "));
        if(!isNull(command.get(1))) {
            playerManager.loadItem(command.get(1), new AudioLoadResultHandler() {
                private final TrackScheduler scheduler = guildMusicManager.scheduler;

                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    scheduler.setFirstAtQueue(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    scheduler.setFirstAtQueueList(audioPlaylist.getTracks());
                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    public static synchronized void deleteElement(MessageCreateEvent event){
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" "));
        if(!isNull(command.get(1)))
        guildMusicManager.scheduler.delete(Integer.parseInt(command.get(1)));
    }
    private synchronized static boolean checkUrl(MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" "));
        if(!isNull(command.get(1)))
        return ContentController.isUrl(command.get(1));
        else return false;
    }
    private synchronized static void skip(MessageCreateEvent event){
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.scheduler.nextTrack();
    }
    private synchronized static void playAndFind(MessageCreateEvent event, String url) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        if(!isNull(url))
        playerManager.loadItemOrdered(guildMusicManager, url, guildMusicManager.resultHandler);
    }
    private synchronized static String getUrl(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" "));
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < command.size() ; i++) {
            builder.append(command.get(i));
        }
        return  Parser.getYoutubeUrl(builder.toString());
    }
    private synchronized static void pause(MessageCreateEvent event){
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.scheduler.setPause(true);
    }
    private synchronized static void resume(MessageCreateEvent event){
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.scheduler.setPause(false);
    }
    private synchronized static void join(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) {
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) {
                    channel.join(spec -> spec.setProvider(guildMusicManager.provider)).block();
                    guildMusicManager.setConnected(true);
                }
            }
        }
    }
    private synchronized static void exit(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) {
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) {
                    channel.sendDisconnectVoiceState().block();
                    guildMusicManager.player.stopTrack();
                    guildMusicManager.setConnected(false);
                }
            }
        }
    }
    private synchronized static void setVolume(MessageCreateEvent event) {
            GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
            final String content = event.getMessage().getContent();
            final List<String> command = Arrays.asList(content.split(" "));
            if(!isNull(command.get(1)))
            guildMusicManager.player.setVolume(Integer.parseInt(command.get(1)));
    }
    private synchronized static Queue<AudioTrack> getQueue(MessageCreateEvent event){
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        //noinspection AccessStaticViaInstance
        return guildMusicManager.scheduler.getQueue();
    }
    private synchronized static boolean isConnectedGuildManager(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        return guildMusicManager.getConnect();
    }
    private synchronized static void clearQueue(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        guildMusicManager.scheduler.clear();
    }
}
