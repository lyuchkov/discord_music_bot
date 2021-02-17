package ru.lyuchkov.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.*;

public class TrackScheduler extends AudioEventAdapter {
    protected static volatile ArrayDeque<AudioTrack> queue = new ArrayDeque<>(30);
    protected final AudioPlayer player;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public static Queue<AudioTrack> getQueue() {
        return queue;
    }


    public void setQueue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.add(track);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public void setPause(Boolean b) {
        player.setPaused(b);
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    public void setListQueue(List<AudioTrack> tracks) {
        queue.addAll(tracks);
        if (player.startTrack(tracks.get(0), true)) {
            nextTrack();
        }
    }

    public void clear() {
        queue.clear();
    }

    public void setFirstAtQueue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.addFirst(track);
        }
    }

    public void setFirstAtQueueList(List<AudioTrack> tracks) {
        for (int i = tracks.size()-1; i >=0 ; i--) {
            queue.addFirst(tracks.get(i));
        }
        if (player.startTrack(tracks.get(0), true)) {
            nextTrack();
        }
    }
    public void delete(int index) {
        List<AudioTrack> tracks = new LinkedList<>();
        for (int i = 0; i < queue.size(); i++) {
            tracks.add(queue.pollLast());
        }
        if (index - 1 < tracks.size()) {
            tracks.remove(index - 1);
            queue.clear();
            queue.addAll(tracks);
        }
    }
}
