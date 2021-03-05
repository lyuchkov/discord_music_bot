package ru.lyuchkov.utils;

public class TimeUtils {
    public static String length(long dur) {
        int sec = (int) (dur / 1000);
        return sec / 60 +
                ":" +
                sec % 60;
    }

}
