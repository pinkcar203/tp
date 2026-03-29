package meditrack.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.BloodGroup;
import meditrack.model.Personnel;
import meditrack.model.Status;

/**
 * Personnel as JSON for Jackson.
 */
public class JsonAdaptedPersonnel {

    public static final String MISSING_FIELD_MSG = "Personnel's %s field is missing.";

    public final String name;
    public final String status;
    public final String bloodGroup;
    public final String allergies;

    /** Jackson calls this when loading the file. */
    @JsonCreator
    public JsonAdaptedPersonnel(
            @JsonProperty("name")       String name,
            @JsonProperty("status")     String status,
            @JsonProperty("bloodGroup") String bloodGroup,
            @JsonProperty("allergies")  String allergies) {
        this.name       = name;
        this.status     = status;
        this.bloodGroup = bloodGroup;
        this.allergies  = allergies;
    }

    /** For saving turns model object into something Jackson can write. */
    public static JsonAdaptedPersonnel fromModelType(Personnel source) {
        return new JsonAdaptedPersonnel(
                source.getName(),
                source.getStatus().name(),
                source.getBloodGroup() != null ? source.getBloodGroup().name() : null,
                source.getAllergies());
    }

    /**
     * Load path validates name/status and maps enums; throws if data looks corrupt.
     */
    public Personnel toModelType() throws CommandException {
        if (name == null || name.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "name"));
        }
        if (status == null || status.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "status"));
        }

        Status parsedStatus;
        try {
            parsedStatus = Status.fromString(status);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Invalid status value in data file: \"" + status + "\"");
        }

        BloodGroup parsedBloodGroup = (bloodGroup != null) ? BloodGroup.fromString(bloodGroup) : null;
        String parsedAllergies = (allergies != null) ? allergies : "";

        return new Personnel(name.trim(), parsedStatus, parsedBloodGroup, parsedAllergies);
    }
}
