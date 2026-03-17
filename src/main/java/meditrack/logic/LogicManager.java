package meditrack.logic;

import java.io.IOException;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.storage.Storage;

/**
 * The main LogicManager of the application.
 * It serves as the central execution engine, handling the execution of commands,
 * enforcing role-based access, and triggering storage saves after state changes.
 */
public class LogicManager implements Logic {
    private final Model model;
    private final Storage storage;

    /**
     * Constructs a {@code LogicManager} with the given Model and Storage.
     *
     * @param model The in-memory model of the application data.
     * @param storage The hard drive storage engine.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
    }

    /**
     * Executes the specified command, enforces authentication, and saves data to the disk.
     *
     * @param command The pre-validated command to execute.
     * @return The result of the command execution to be displayed in the UI.
     * @throws CommandException If the user is not logged in, lacks permission, or if the execution fails.
     */
    @Override
    public CommandResult executeCommand(Command command) throws CommandException {
        // 1. Role Enforcement Check
        Role currentRole = model.getSession().getRole();
        if (currentRole == null) {
            throw new CommandException("Security Error: No active session. Please log in.");
        }

        // (Note: Person B and C can add specific role checks inside their individual
        // Command classes, but we ensure here that they are at least logged in!)

        // 2. Execute the command against the model
        CommandResult commandResult = command.execute(model);

        // 3. Trigger save via Storage after a successful execution
        try {
            storage.saveMediTrackData((meditrack.model.ReadOnlyMediTrack) model);
        } catch (IOException ioe) {
            throw new CommandException("Could not save data to file: " + ioe.getMessage());
        }

        return commandResult;
    }
}