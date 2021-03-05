package ru.lyuchkov.factories;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.object.entity.Guild;
import ru.lyuchkov.containers.MusicManagerContainer;
import ru.lyuchkov.player.GuildMusicManager;

public class GuildMusicManagerFactory {
    public static synchronized GuildMusicManager getGuildPlayerManager(Guild guild) {
        long guildId = guild.getId().asLong();
        GuildMusicManager musicManager = MusicManagerContainer.getMusicManager(guildId);
        if (musicManager == null) {
            createGuildPlayerManager(guild);
            musicManager = getGuildPlayerManager(guild);
            MusicManagerContainer.put(guildId, musicManager);
        }
        return musicManager;
    }
    public static synchronized void createGuildPlayerManager(Guild guild) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        MusicManagerContainer.put(guild.getId().asLong(), new GuildMusicManager(playerManager));
    }
}
