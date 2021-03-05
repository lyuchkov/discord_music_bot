package ru.lyuchkov.containers.command;

import ru.lyuchkov.handlers.Command;

import java.util.Map;

public interface CommandContainer {
    Map<String, Command> getCommands();
}
