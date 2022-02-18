package ru.lyuchkov.handlers;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.containers.command.*;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements Handler {
    private final Map<String, Command> commands;

    public CommandHandler() {
        PassCommandContainer passCommandContainer = new PassCommandContainer();
        PlayCommandContainer playCommandContainer = new PlayCommandContainer();
        QueueCommandContainer queueCommandContainer = new QueueCommandContainer();
        FeedbackCommandContainer feedbackCommandContainer = new FeedbackCommandContainer();
        BreakCommandContainer breakCommandContainer = new BreakCommandContainer();
        VolumeCommandContainer volumeCommandContainer = new VolumeCommandContainer();
        commands = Stream.of(passCommandContainer.getCommands(), playCommandContainer.getCommands(), queueCommandContainer.getCommands(), feedbackCommandContainer.getCommands(),
                breakCommandContainer.getCommands(), volumeCommandContainer.getCommands())
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void handle(MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        try {
            for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                if (content.startsWith('$' + entry.getKey())) {
                    entry.getValue().execute(event);
                    break;
                }
            }
        } catch (Exception e) {
            Objects.requireNonNull(event.getMessage().getChannel().block()).createMessage("Произошла какая-то ошибка.");
        }
    }

}
