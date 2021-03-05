package ru.lyuchkov;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import ru.lyuchkov.handlers.CommandHandler;

public class Bot {

    public static void main(String[] args) {
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();
        assert client != null;
        CommandHandler commandHandler = new CommandHandler();
        client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .subscribe(commandHandler::handle);
        client.onDisconnect().block();
    }


}