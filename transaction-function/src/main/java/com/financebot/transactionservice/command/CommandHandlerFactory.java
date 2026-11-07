package com.financebot.transactionservice.command;

import com.financebot.transactionservice.command.CommandHandler;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.enterprise.inject.Instance;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommandHandlerFactory {

    private final Map<String, CommandHandler> handlers;

    public CommandHandlerFactory(Instance<CommandHandler> commandHandlers) {
        this.handlers = commandHandlers.stream()
                .collect(Collectors.toMap(CommandHandler::getCommand, Function.identity()));
    }

    public CommandHandler getHandler(String command) {
        return handlers.getOrDefault(command, handlers.get("/default"));
    }
}
