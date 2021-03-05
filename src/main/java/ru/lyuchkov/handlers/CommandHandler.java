package ru.lyuchkov.handlers;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.containers.command.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler implements Handler {
    private final Map<String, Command> commands = new ConcurrentHashMap<>();

    public CommandHandler() {
        PassCommandContainer passCommandContainer = new PassCommandContainer();
        PlayCommandContainer playCommandContainer = new PlayCommandContainer();
        QueueCommandContainer queueCommandContainer = new QueueCommandContainer();
        FeedbackCommandContainer feedbackCommandContainer = new FeedbackCommandContainer();
        BreakCommandContainer breakCommandContainer = new BreakCommandContainer();
        VolumeCommandContainer volumeCommandContainer = new VolumeCommandContainer();
        commands.putAll(passCommandContainer.getCommands());
        commands.putAll(playCommandContainer.getCommands());
        commands.putAll(queueCommandContainer.getCommands());
        commands.putAll(feedbackCommandContainer.getCommands());
        commands.putAll(breakCommandContainer.getCommands());
        commands.putAll(volumeCommandContainer.getCommands());
    }
    @Override
    public void handle(MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        for (final Map.Entry<String, Command> entry : commands.entrySet()) {
            if (content.startsWith('$' + entry.getKey())) {
                entry.getValue().execute(event);
                break;
            }
        }
    }

}
