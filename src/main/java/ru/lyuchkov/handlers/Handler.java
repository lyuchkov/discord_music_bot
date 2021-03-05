package ru.lyuchkov.handlers;

import discord4j.core.event.domain.message.MessageCreateEvent;


public interface Handler {
    void handle(MessageCreateEvent e);
}
