package meditrack.logic.commands.personnel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Role;

/**
 * Shuffles FIT personnel into a duty order. Not saved to the model;
 * UI reads {@link #getLastRoster()} after run.
 */
public class GenerateRosterCommand extends Command {

    public static final String COMMAND_WORD = "generate_roster";

    public static final String MESSAGE_SUCCESS_HEADER = "Duty Roster — %d FIT personnel:";
    public static final String MESSAGE_USAGE =
            COMMAND_WORD + ": Generates a randomised duty roster from all FIT personnel.\n"
                    + "No parameters required.\n"
                    + "Example: " + COMMAND_WORD;

    private List<Personnel> lastRoster;

    /** Creates a command; the roster is produced when {@link #execute} runs. */
    public GenerateRosterCommand() {
    }

    /** Shuffles FIT personnel and returns the roster text. */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        ModelManager manager = (ModelManager) model;
        lastRoster = manager.generateRoster();

        String header = String.format(MESSAGE_SUCCESS_HEADER, lastRoster.size());
        String numberedList = IntStream.range(0, lastRoster.size())
                .mapToObj(i -> (i + 1) + ". " + lastRoster.get(i).getName())
                .collect(Collectors.joining("\n"));

        return new CommandResult(header + "\n" + numberedList);
    }

    /** Platoon commander only. */
    @Override
    public Role getRequiredRole() {
        return Role.PLATOON_COMMANDER;
    }

    /** Last roster from {@link #execute}, or {@code null} if not run yet. */
    public List<Personnel> getLastRoster() {
        return lastRoster;
    }
}