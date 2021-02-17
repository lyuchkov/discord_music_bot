package ru.lyuchkov.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Queue;

public class OutputUtils {
    public static String printQueue(Queue<AudioTrack> deque) {
        StringBuilder builder = new StringBuilder();
        String first = "На очереди: \n";
        builder.append(first);
        int c = 1;
        for (AudioTrack a :
                deque) {
            builder.append(c).append(". ").append(a.getInfo().title).append("\n");
            c++;
        }
        if (builder.toString().equals(first))
            return "Очередь пуста";
        else
            return builder.toString();
    }

    public static String printCommands() {
        String first = "Список команд: \n";
        return first +
                "#play" + "\n" +
                "#pause" + "\n" +
                "#resume" + "\n" +
                "#fs" + "\n" +
                "#join" + "\n" +
                "#exit" + "\n" +
                "#q" + "\n" +
                "#help" + "\n";
    }

}
