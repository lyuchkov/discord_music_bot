package ru.lyuchkov.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {

    private final TrackScheduler scheduler;

    public AudioLoadResultHandlerImpl(TrackScheduler scheduler) {
        this.scheduler = scheduler;
    }


    @Override
    public void trackLoaded(AudioTrack track) {
        scheduler.setQueue(track);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        scheduler.setListQueue(playlist.getTracks());
    }

    @Override
    public void noMatches() {

    }


    @Override
    public void loadFailed(final FriendlyException exception) {
        exception.printStackTrace();
    }


}