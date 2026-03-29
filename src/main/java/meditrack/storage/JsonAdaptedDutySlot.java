package meditrack.storage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.DutySlot;
import meditrack.model.DutyType;

/** One duty slot line in JSON. */
public class JsonAdaptedDutySlot {

    public static final String MISSING_FIELD_MSG = "DutySlot's %s field is missing.";

    public final String date;
    public final String startTime;
    public final String endTime;
    public final String dutyType;
    public final String personnelName;

    @JsonCreator
    public JsonAdaptedDutySlot(@JsonProperty("date") String date,
                                @JsonProperty("startTime") String startTime,
                                @JsonProperty("endTime") String endTime,
                                @JsonProperty("dutyType") String dutyType,
                                @JsonProperty("personnelName") String personnelName) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dutyType = dutyType;
        this.personnelName = personnelName;
    }

    public static JsonAdaptedDutySlot fromModelType(DutySlot source) {
        return new JsonAdaptedDutySlot(
                source.getDate().toString(),
                source.getStartTime().toString(),
                source.getEndTime().toString(),
                source.getDutyType().name(),
                source.getPersonnelName());
    }

    /** Converts this DTO back to a domain DutySlot. */
    public DutySlot toModelType() throws CommandException {
        if (startTime == null || startTime.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "startTime"));
        }
        if (endTime == null || endTime.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "endTime"));
        }
        if (dutyType == null || dutyType.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "dutyType"));
        }
        if (personnelName == null || personnelName.isBlank()) {
            throw new CommandException(String.format(MISSING_FIELD_MSG, "personnelName"));
        }

        LocalDate slotDate = LocalDate.now();
        if (date != null && !date.isBlank()) {
            try {
                slotDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                throw new CommandException("Invalid date format in duty slot: expected YYYY-MM-DD.");
            }
        }

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startTime);
            end = LocalTime.parse(endTime);
        } catch (DateTimeParseException e) {
            throw new CommandException("Invalid time format in duty slot: expected HH:mm.");
        }

        DutyType type;
        try {
            type = DutyType.valueOf(dutyType);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Invalid duty type in data file: \"" + dutyType + "\"");
        }

        return new DutySlot(slotDate, start, end, type, personnelName);
    }
}
