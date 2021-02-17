package ru.lyuchkov.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;

public class UrlUtils {
    public static boolean isUrl(String toCheck){
        String[] temp = toCheck.split("/");
        return temp[0].equals("https:") || temp[0].equals("http:");
    }
    public  static boolean checkUrl(MessageCreateEvent event) {
        final String content = event.getMessage().getContent().replaceAll("\t", " ")
                .replaceAll("\n", " ");
        if (content.equals("*p")) {
            return false;
        }else {
            final List<String> command = Arrays.asList(content.split(" "));
            if (!isNull(command.get(1)))
                return UrlUtils.isUrl(command.get(1));
            else return false;
        }
    }
    public static boolean isNull(String query){
        return query == null;
    }
}
