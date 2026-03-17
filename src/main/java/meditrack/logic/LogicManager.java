package meditrack.logic;

import java.io.IOException;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.storage.Storage;

/**
 * The main LogicManager of the app.
 * It handles the execution of commands, enforces role-based access, and triggers storage saves.
 */
public class LogicManager implements Logic {
    private final Model model;
    private final Storage storage;

    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
    }

    @Override
    public CommandResult executeCommand(Command command) throws CommandException {
        // TODO: Add role enforcement check using model.getSession().getRole() before execution

        // Execute the command against the model
        CommandResult commandResult = command.execute(model);

        // TODO: Trigger save via storage.saveMediTrackData(model) after execution
        // try {
        //     storage.saveMediTrackData((meditrack.model.ReadOnlyMediTrack) model);
        // } catch (IOException ioe) {
        //     throw new CommandException("Could not save data to file: " + ioe.getMessage());
        // }

        return commandResult;
    }
}