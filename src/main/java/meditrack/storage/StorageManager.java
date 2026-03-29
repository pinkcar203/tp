package meditrack.storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.DutySlot;
import meditrack.model.MediTrack;
import meditrack.model.Personnel;
import meditrack.model.ReadOnlyMediTrack;

/**
 * Saves and loads MediTrack data to data.json via Jackson.
 */
public class StorageManager implements Storage {

    private final JsonMediTrackStorage jsonStorage;

    /** Cached password hash so saves never need to re-read the file. */
    private String cachedPasswordHash;

    /** Creates a storage manager using the default JSON file path. */
    public StorageManager() {
        this.jsonStorage = new JsonMediTrackStorage();
    }

    /** True if data.json does not exist yet. */
    @Override
    public boolean isFirstLaunch() {
        return !jsonStorage.getFilePath().toFile().exists();
    }

    /** Loads from data.json; skips bad rows. */
    @Override
    public Optional<ReadOnlyMediTrack> readMediTrackData() {
        Optional<JsonSerializableMediTrack> rawData = jsonStorage.readData();
        if (rawData.isEmpty()) {
            return Optional.empty();
        }

        JsonSerializableMediTrack serializable = rawData.get();
        cachedPasswordHash = serializable.passwordHash;

        MediTrack mediTrack = new MediTrack();

        for (JsonAdaptedPersonnel adapted : serializable.personnel) {
            try {
                Personnel p = adapted.toModelType();
                mediTrack.addPersonnelRecord(p);
            } catch (CommandException e) {
                System.err.println("[StorageManager] Skipping corrupt personnel record: "
                        + e.getMessage());
            }
        }

        for (JsonAdaptedSupply adapted : serializable.supplies) {
            try {
                mediTrack.addSupplyRecord(adapted.toModelType());
            } catch (CommandException e) {
                System.err.println("[StorageManager] Skipping corrupt supply record: "
                        + e.getMessage());
            }
        }

        for (JsonAdaptedDutySlot adapted : serializable.dutySlots) {
            try {
                mediTrack.addDutySlotRecord(adapted.toModelType());
            } catch (CommandException e) {
                System.err.println("[StorageManager] Skipping corrupt duty slot record: "
                        + e.getMessage());
            }
        }

        return Optional.of(mediTrack);
    }

    /** Writes data to data.json. */
    @Override
    public void saveMediTrackData(ReadOnlyMediTrack data) throws IOException {
        List<JsonAdaptedPersonnel> adaptedPersonnel = data.getPersonnelList()
                .stream()
                .map(JsonAdaptedPersonnel::fromModelType)
                .toList();
        List<JsonAdaptedSupply> adaptedSupplies = data.getSupplyList()
                .stream()
                .map(JsonAdaptedSupply::fromModelType)
                .toList();

        List<JsonAdaptedDutySlot> adaptedDutySlots = data.getDutySlots()
                .stream()
                .map(JsonAdaptedDutySlot::fromModelType)
                .toList();

        if (cachedPasswordHash == null) {
            cachedPasswordHash = jsonStorage.readData()
                    .map(d -> d.passwordHash)
                    .orElse(null);
        }

        JsonSerializableMediTrack serializableData =
                new JsonSerializableMediTrack(cachedPasswordHash, adaptedSupplies,
                        adaptedPersonnel, adaptedDutySlots);

        jsonStorage.saveData(serializableData);
    }
}
