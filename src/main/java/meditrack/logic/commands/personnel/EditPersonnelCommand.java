package meditrack.logic.commands.personnel;

import java.util.List;
import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.BloodGroup;
import meditrack.model.Model;
import meditrack.model.Personnel;
import meditrack.model.Role;

public class EditPersonnelCommand extends Command {
    private final int targetIndex; // 1-based index
    private final BloodGroup updatedBloodGroup;
    private final String updatedAllergies;

    public EditPersonnelCommand(int targetIndex, BloodGroup updatedBloodGroup, String updatedAllergies) {
        this.targetIndex = targetIndex;
        this.updatedBloodGroup = updatedBloodGroup;
        this.updatedAllergies = updatedAllergies;
    }

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

    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.MEDICAL_OFFICER);
    }
}