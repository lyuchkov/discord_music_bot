package ru.lyuchkov.utils;

public final class UrlUtils {
    public static boolean isUrl(String toCheck){
        String[] temp = toCheck.split("/");
        return temp[0].equals("https:") || temp[0].equals("http:");
    }
}
