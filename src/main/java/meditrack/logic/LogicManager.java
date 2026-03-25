package meditrack.logic;

import java.io.IOException;
import java.util.List;
import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.storage.Storage;

/**
 * The main LogicManager of the app.
 * It handles the execution of commands, enforces role-based access, and triggers storage saves.
 */
public class LogicManager implements Logic {
    private final Model model;
    private final Storage storage;

    /**
     * @param model in-memory state
     * @param storage persistence layer
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
    }

    /** Runs the command, checks role, then saves to storage. */
    @Override
    public CommandResult executeCommand(Command command) throws CommandException {
        List<Role> allowedRoles = command.getRequiredRoles();

        if (allowedRoles != null && !allowedRoles.isEmpty()) {
            Role currentRole = model.getSession().getRole();

            if (currentRole == null || !allowedRoles.contains(currentRole)) {
                throw new CommandException("You do not have permission to execute this command. "
                        + "Allowed roles: " + allowedRoles);
            }
        }

        CommandResult commandResult = command.execute(model);

        try {
            storage.saveMediTrackData(model.getMediTrack());
        } catch (IOException ioe) {
            throw new CommandException("Could not save data to file: " + ioe.getMessage());
        }

        return commandResult;
    }
}
