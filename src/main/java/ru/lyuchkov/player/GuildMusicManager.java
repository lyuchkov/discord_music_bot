package ru.lyuchkov.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
    public final AudioPlayer player;
    public final AudioLoadResultHandlerImpl resultHandler;
    public final LavaPlayerAudioProvider provider;
    public static boolean connected;
    public final TrackScheduler scheduler;


    public GuildMusicManager(AudioPlayerManager playerManager) {
        player = playerManager.createPlayer();
        provider = new LavaPlayerAudioProvider(player);
        scheduler = new TrackScheduler(player);
        resultHandler = new AudioLoadResultHandlerImpl(scheduler);
        connected = false;
    }
    public void setConnected(Boolean t){
        connected=t;
    }
    public boolean isConnected(){
        return !connected;
    }

}
