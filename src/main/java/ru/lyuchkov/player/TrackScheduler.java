package ru.lyuchkov.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public final class TrackScheduler implements AudioLoadResultHandler, AudioEventListener {

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingDeque<>();
    }


    @Override
    public void trackLoaded(final AudioTrack track) {
        queue.add(track);
        try {
            player.playTrack(queue.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        List<AudioTrack> play = playlist.getTracks();
        queue.addAll(play);
        try {
            player.playTrack(queue.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
            exception.printStackTrace();
    }

    @Override
    public void onEvent(AudioEvent audioEvent) {

    }
}