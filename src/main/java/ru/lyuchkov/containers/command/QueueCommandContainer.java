package ru.lyuchkov.containers.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.factories.GuildMusicManagerFactory;
import ru.lyuchkov.handlers.Command;
import ru.lyuchkov.player.GuildMusicManager;
import ru.lyuchkov.player.TrackScheduler;
import ru.lyuchkov.utils.InputUtils;
import ru.lyuchkov.utils.OutputUtils;
import ru.lyuchkov.utils.TimeUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class QueueCommandContainer implements CommandContainer {
    public static synchronized void deleteElement(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        final String numAsStr = InputUtils.getValidCommand("remove", event.getMessage().getContent()).replaceAll(" ", "");
        int position;
        int size = TrackScheduler.getQueue().size();
        if (!numAsStr.isEmpty()) {
            try {
                position = Integer.parseInt(numAsStr);
                if (position > size || position < 1) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block())
                            .createMessage("Тебя в школе считать не научили?").block();
                } else {
                    guildMusicManager.scheduler.delete(position - 1);
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block())
                            .createMessage("Удалил.").block();
                }
            } catch (NumberFormatException e) {
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block())
                        .createMessage("Некорректный формат команды.").block();
            }
        }
    }
    public synchronized static void clearQueue(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.clear();
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Теперь очередь пуста").block();
    }
    public synchronized static void loop(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.loop();
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Теперь будешь только это слушать").block();
    }

    public synchronized static void unLoop(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.unLoop();
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Надоело?").block();
    }
    public synchronized static void printQueue(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage(OutputUtils.printQueue(TrackScheduler.getQueue())).block();
    }

    public synchronized static void printNowPlay(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        String title = guildMusicManager.player.getPlayingTrack().getInfo().title;
        String length = TimeUtils.length(guildMusicManager.player.getPlayingTrack().getDuration());
        String nowPosition = TimeUtils.length(guildMusicManager.player.getPlayingTrack().getPosition());
        if (title != null) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("♫Сейчас играет♫" + "\n"
                            + title + "\n" + "\n"
                            + "Времени прошло: " + nowPosition +  " мин."+"\n"
                            + "Трек длится: " +length+  " мин.").block();
        } else {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("Сейчас ничего не играет.").block();
        }
    }
    public synchronized static void skip(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = GuildMusicManagerFactory.getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.nextTrack();
    }
    public synchronized static void fastSkip(MessageCreateEvent event) {
        skip(event);
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Мне тоже не нравится").block();
    }

    @Override
    public Map<String, Command> getCommands() {
        ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
        commands.put("remove", QueueCommandContainer::deleteElement);
        commands.put("clr", QueueCommandContainer::clearQueue);
        commands.put("loop", QueueCommandContainer::loop);
        commands.put("unloop", QueueCommandContainer::unLoop);
        commands.put("q", QueueCommandContainer::printQueue);
        commands.put("np", QueueCommandContainer::printNowPlay);
        commands.put("fs", QueueCommandContainer::fastSkip);
        return commands;
    }
}
