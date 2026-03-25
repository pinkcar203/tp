package meditrack.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Personnel;
import meditrack.model.Status;

/**
 * Jackson-friendly version of a {@link Personnel} record.
 *
 * <p>Extends the team's existing stub with:
 * <ul>
 *   <li>{@link #toModelType()} — converts this DTO back to a domain {@link Personnel}</li>
 *   <li>{@link #fromModelType(Personnel)} — factory that builds this DTO from a domain object</li>
 * </ul>
 *
 * <p>JSON format:
 * <pre>
 * {
 *   "name": "John Tan",
 *   "status": "FIT"
 * }
 * </pre>
 */
public class JsonAdaptedPersonnel {
    public static final String MISSING_FIELD_MSG = "Personnel's %s field is missing.";
    public final String name;
    public final String status;

    /**
     * Constructs a {@code JsonAdaptedPersonnel} from raw JSON fields.
     * Used automatically by Jackson during deserialisation.
     */
    @JsonCreator
    public JsonAdaptedPersonnel(@JsonProperty("name") String name,
                                @JsonProperty("status") String status) {
        this.name = name;
        this.status = status;
    }

    /**
     * Converts a domain {@link Personnel} object into its JSON-friendly form.
     * Use this when saving to file.
     *
     * @param source the domain object to serialise
     * @return the corresponding JSON DTO
     */
    public static JsonAdaptedPersonnel fromModelType(Personnel source) {
        return new JsonAdaptedPersonnel(
                source.getName(),
                source.getStatus().name()); // store enum name, e.g. "FIT"
    }

    /**
     * Converts this JSON DTO back into a domain {@link Personnel} object.
     * Use this when loading from file.
     *
     * @return the corresponding domain {@link Personnel}
     * @throws CommandException if any stored field is null, blank, or holds an invalid Status string
     */
    public Personnel toModelType() throws CommandException {
        // Validate name
        if (name == null || name.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "name"));
        }

        // Validate and parse status
        if (status == null || status.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "status"));
        }
        Status parsedStatus;
        try {
            parsedStatus = Status.fromString(status);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Invalid status value in data file: \"" + status + "\"");
        }

        return new Personnel(name.trim(), parsedStatus);
    }
}