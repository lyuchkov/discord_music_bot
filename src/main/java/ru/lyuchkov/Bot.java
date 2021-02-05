package ru.lyuchkov;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import ru.lyuchkov.interfaces.Command;
import ru.lyuchkov.player.LavaPlayerAudioProvider;
import ru.lyuchkov.player.TrackScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Bot {
    private static final ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
    private static boolean connected = false;
    private static final List<String> history = new ArrayList<>();

    public static void main(String[] args) {
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();
        assert client != null;
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        final AudioPlayer player = playerManager.createPlayer();
        AudioProvider provider = new LavaPlayerAudioProvider(player);
        final TrackScheduler scheduler = new TrackScheduler(player);
        putCommands(playerManager, provider, scheduler);
        client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .subscribe(event -> {
                    final String content = event.getMessage().getContent();
                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        if (content.startsWith('#' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });
        client.onDisconnect().block();
    }
   private static void putCommands(AudioPlayerManager playerManager, AudioProvider provider, TrackScheduler scheduler){
       commands.put("play", event -> {
           if (!connected)
               join(provider, event);
           play(scheduler, playerManager, event);
       });
       commands.put("join", event -> {
           Objects.requireNonNull(event.getMessage()
                   .getChannel().block()).
                   createMessage("I'm here").block();
           join(provider, event);
       });
       commands.put("history", event ->  Objects.requireNonNull(event.getMessage()
                   .getChannel().block())
                   .createMessage(history.toString()).block());

   }

    private synchronized static void play(TrackScheduler scheduler, AudioPlayerManager playerManager, MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" "));
        history.add(event.getMessage().getContent());
        playerManager.loadItem(command.get(1), scheduler);
    }

    private synchronized static void join(AudioProvider provider, MessageCreateEvent event) {
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) {
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) {
                    channel.join(spec -> spec.setProvider(provider)).block();
                    connected = true;
                }
            }
        }
    }
}
