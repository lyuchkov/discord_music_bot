package ru.lyuchkov.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.*;

public class TrackScheduler extends AudioEventAdapter {
    protected static volatile LinkedList<AudioTrack> queue = new LinkedList<>();
    protected final AudioPlayer player;
    protected static boolean isLoop = false;
    protected static AudioTrack loops;
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public static Queue<AudioTrack> getQueue() {
        return queue;
    }


    public void setQueue(AudioTrack track) {
        if(queue.size()+1 <=100) {
            if (!player.startTrack(track, true)) {
                queue.add(track);
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (isLoop) {
                player.startTrack(loops.makeClone(), false);
            } else {
                nextTrack();
            }
        }
    }

    public void setPause(Boolean b) {
        player.setPaused(b);
    }

    public void nextTrack() {
        if(isLoop){
            player.startTrack(loops.makeClone(), false);
        }else {
            player.startTrack(queue.poll(), false);
        }
    }
    public void setListQueue(List<AudioTrack> tracks) {
        if(tracks.size() + queue.size() <=100) {
            if (player.startTrack(tracks.get(0), true)) {
                queue.addAll(tracks);
            }
        }
    }

    public void clear() {
        queue.clear();
    }

    public void setFirstAtQueue(AudioTrack track) {
        if(queue.size()+1 <=100) {
            if (!player.startTrack(track, true)) {
                queue.addFirst(track);
            }
        }
    }

    public void setFirstAtQueueList(List<AudioTrack> tracks) {
        if(tracks.size() + queue.size() <=100) {
            for (int i = tracks.size() - 1; i >= 0; i--) {
                queue.addFirst(tracks.get(i));
            }
            if (player.startTrack(tracks.get(0), true)) {
                nextTrack();
            }
        }
    }
    public void delete(int index) {
        queue.remove(index);
    }

    public void loop() {
        isLoop = true;
        loops = player.getPlayingTrack().makeClone();
    }
    public void unLoop() {
        isLoop = false;
        loops = null;
    }
}
