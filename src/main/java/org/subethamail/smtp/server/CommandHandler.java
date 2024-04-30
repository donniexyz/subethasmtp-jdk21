package org.subethamail.smtp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.DropConnectionException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * This class manages execution of a SMTP command.
 *
 * @author Jon Stevens
 * @author Scott Hernandez
 * @author Dony Zulkarnaen
 */
@SuppressWarnings("unused")
public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    /**
     * The map of known SMTP commands. Keys are upper case names of the
     * commands.
     */
    private final Map<String, Command> commandMap = new HashMap<>();

    /**
     *
     */
    public CommandHandler() {
        // This solution should be more robust than the earlier "manual" configuration.
        for (CommandRegistry registry : CommandRegistry.values()) {
            this.addCommand(registry.getCommand());
        }
    }

    /**
     * Create a command handler with a specific set of commands.
     *
     * @param availableCommands the available commands (not null)
     *                          TLS note: wrap commands with {@link RequireTLSCommandWrapper} when appropriate.
     */
    public CommandHandler(Collection<Command> availableCommands) {
        for (Command command : availableCommands) {
            this.addCommand(command);
        }
    }

    /**
     * Adds or replaces the specified command.
     */
    public void addCommand(Command command) {
        if (log.isDebugEnabled())
            log.debug("Added command: {}", command.getName());

        this.commandMap.put(command.getName(), command);
    }

    /**
     * Returns the command object corresponding to the specified command name.
     *
     * @param commandName case insensitive name of the command.
     * @return the command object, or null, if the command is unknown.
     */
    public Command getCommand(String commandName) {
        String upperCaseCommandName = commandName.toUpperCase(Locale.ENGLISH);
        return this.commandMap.get(upperCaseCommandName);
    }

    /**
     *
     */
    public boolean containsCommand(String command) {
        return this.commandMap.containsKey(command);
    }

    /**
     *
     */
    public Set<String> getVerbs() {
        return this.commandMap.keySet();
    }

    /**
     *
     */
    public void handleCommand(Session context, String commandString)
            throws SocketTimeoutException, IOException, DropConnectionException {
        try {
            Command command = this.getCommandFromString(commandString);
            command.execute(commandString, context);
        } catch (CommandException e) {
            context.sendResponse("500 " + e.getMessage());
        }
    }

    /**
     * @return the HelpMessage object for the given command name (verb)
     * @throws CommandException
     */
    public HelpMessage getHelp(String command) throws CommandException {
        return this.getCommandFromString(command).getHelp();
    }

    /**
     *
     */
    private Command getCommandFromString(String commandString)
            throws UnknownCommandException, InvalidCommandNameException {
        Command command = null;
        String[] keyVerb = splitKeyVerb(commandString);
        String key = keyVerb[0];
        if (!key.isEmpty()) {
            command = this.commandMap.get(key);
        }
        if (command == null) {
            // some commands have a verb longer than 4 letters
            String verb = keyVerb[1];
            if (verb.isEmpty()) {
                command = this.commandMap.get(verb);
            }
        }
        if (command == null) {
            throw new UnknownCommandException("Error: command not implemented");
        }
        return command;
    }

    private String[] splitKeyVerb(String string) throws InvalidCommandNameException {
        if (string == null || string.length() < 4)
            throw new InvalidCommandNameException("Error: bad syntax");
        String[] split = CommandRegistry.split(string);
        return split;
    }
}
