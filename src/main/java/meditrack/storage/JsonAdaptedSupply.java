package meditrack.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Supply;

/**
 * Jackson-friendly version of a Supply item.
 * Used to serialize and deserialize Supply objects to and from JSON format.
 */
public class JsonAdaptedSupply {

    public static final String MISSING_FIELD_MSG = "Supply's %s field is missing.";

    public final String name;
    public final int quantity;
    public final String expiryDate;

    @JsonCreator
    public JsonAdaptedSupply(@JsonProperty("name") String name,
                             @JsonProperty("quantity") int quantity,
                             @JsonProperty("expiryDate") String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    /**
     * Converts a domain Supply object into its JSON-friendly form.
     */
    public static JsonAdaptedSupply fromModelType(Supply source) {
        return new JsonAdaptedSupply(
                source.getName(),
                source.getQuantity(),
                source.getExpiryDate().toString());
    }

    /**
     * Converts this JSON DTO back into a domain Supply object.
     *
     * @throws CommandException if any stored field is null, blank, or invalid
     */
    public Supply toModelType() throws CommandException {
        if (name == null || name.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "name"));
        }
        if (expiryDate == null || expiryDate.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "expiryDate"));
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(expiryDate);
        } catch (DateTimeParseException e) {
            throw new CommandException("Invalid expiry date in data file: \"" + expiryDate + "\"");
        }

        return new Supply(name.trim(), quantity, parsedDate);
    }
}
