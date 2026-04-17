package meditrack.logic.commands.personnel;

import java.util.List;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.BloodGroup;
import meditrack.model.Model;
import meditrack.model.Personnel;
import meditrack.model.Role;

/**
 * Edits medical profile fields (blood group, allergies) for someone already on the roster.
 * Index is the 1-based row from the personnel table.
 */
public class EditPersonnelCommand extends Command {

    private final int targetIndex; // 1-based index
    private final BloodGroup updatedBloodGroup;
    private final String updatedAllergies;

    /**
     * @param targetIndex        1-based index in the current personnel list.
     * @param updatedBloodGroup  new blood group (or null if cleared).
     * @param updatedAllergies   allergy text to store.
     */
    public EditPersonnelCommand(int targetIndex, BloodGroup updatedBloodGroup, String updatedAllergies) {
        this.targetIndex = targetIndex;
        this.updatedBloodGroup = updatedBloodGroup;
        this.updatedAllergies = updatedAllergies;
    }

    /**
     * @param model the app model (must expose the same personnel list the UI shows).
     * @return success message with the person's name.
     * @throws CommandException if the index is out of range.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Personnel> lastShownList = model.getPersonnelList();

        if (targetIndex < 1 || targetIndex > lastShownList.size()) {
            throw new CommandException("The personnel index provided is invalid.");
        }

        Personnel personToEdit = lastShownList.get(targetIndex - 1);

        Personnel editedPerson = new Personnel(
                personToEdit.getName(),
                personToEdit.getStatus(),
                updatedBloodGroup,
                updatedAllergies,
                personToEdit.getStatusExpiryDate(),
                personToEdit.getLastModified()
        );

        model.setPersonnel(personToEdit, editedPerson);
        return new CommandResult("Successfully updated medical details for: " + editedPerson.getName());
    }

    /**
     * @return only Medical Officers may edit profiles.
     */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.MEDICAL_OFFICER);
    }
}
