package ru.lyuchkov.utils;

import java.util.ArrayList;
import java.util.List;

public class InputUtils {
    public static String getValidCommand(String name, String command){
        return getValidString(command).replaceAll("\\$"+name, "");
    }
    public static String getValidString(String command){
        return command.replaceAll("\t", "")
                .replaceAll("\n", "")
                .trim();
    }
    public static List<String> getValidList(String str){
        List<String> result = new ArrayList<>();
        String[] split = str.split(" ");
        for (String s : split) {
            if (!s.isEmpty()) {
                result.add(s);
            }
        }
        return result;
    }
}
