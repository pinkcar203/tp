package meditrack.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An Immutable wrapper class that holds all MediTrack data for Jackson serialization.
 * This class serves as the root JSON object containing the password hash, supplies, and personnel.
 */
public class JsonSerializableMediTrack {
    public final String passwordHash;
    public final List<JsonAdaptedSupply> supplies = new ArrayList<>();
    public final List<JsonAdaptedPersonnel> personnel = new ArrayList<>();
    public final List<JsonAdaptedDutySlot> dutySlots = new ArrayList<>();

    /**
     * Constructs a JsonSerializableMediTrack with the given data.
     *
     * @param passwordHash The BCrypt hash of the master application password.
     * @param supplies     A list of serialized supply items.
     * @param personnel    A list of serialized personnel records.
     * @param dutySlots    A list of serialized duty slots (may be null for old data files).
     */
    @JsonCreator
    public JsonSerializableMediTrack(@JsonProperty("passwordHash") String passwordHash,
                                     @JsonProperty("supplies") List<JsonAdaptedSupply> supplies,
                                     @JsonProperty("personnel") List<JsonAdaptedPersonnel> personnel,
                                     @JsonProperty("dutySlots") List<JsonAdaptedDutySlot> dutySlots) {
        this.passwordHash = passwordHash;
        if (supplies != null) {
            this.supplies.addAll(supplies);
        }
        if (personnel != null) {
            this.personnel.addAll(personnel);
        }
        if (dutySlots != null) {
            this.dutySlots.addAll(dutySlots);
        }
    }
}