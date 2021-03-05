package ru.lyuchkov.containers;

import ru.lyuchkov.player.GuildMusicManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManagerContainer {
    @SuppressWarnings("FieldMayBeFinal")
    private static Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    public static GuildMusicManager getMusicManager(long guildId){
        return musicManagers.get(guildId);
    }

    public static void put(long guildId, GuildMusicManager guildMusicManager) {
        musicManagers.put(guildId, guildMusicManager);
    }
}
