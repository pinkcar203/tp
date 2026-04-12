package meditrack.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import meditrack.model.BloodGroup;
import meditrack.model.DutySlot;
import meditrack.model.DutyType;
import meditrack.model.MediTrack;
import meditrack.model.Personnel;
import meditrack.model.ReadOnlyMediTrack;
import meditrack.model.Status;
import meditrack.model.Supply;



public class StorageManagerTest {

    @TempDir
    public Path testFolder;

    private StorageManager storageManager;
    private MediTrack testModel;

    @BeforeEach
    public void setUp() {
        // Inject a temporary file path so we don't overwrite the real data.json
        Path tempFilePath = testFolder.resolve("test_data.json");
        JsonMediTrackStorage jsonStorage = new JsonMediTrackStorage(tempFilePath);
        storageManager = new StorageManager(jsonStorage);

        testModel = new MediTrack();
    }

    @Test
    public void readMediTrackData_fileDoesNotExist_returnsEmptyOptional() {
        // Act: Try reading from a fresh temp path that hasn't been written to
        Optional<ReadOnlyMediTrack> result = storageManager.readMediTrackData();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void saveAndReadMediTrackData_validModel_readsCorrectly() throws Exception {
        // Arrange: Populate the model with data
        testModel.addPersonnelRecord(new meditrack.model.Personnel("Test Soldier", Status.FIT));

        // Act: Save it, then read it back
        storageManager.saveMediTrackData(testModel);
        Optional<ReadOnlyMediTrack> loadedResult = storageManager.readMediTrackData();

        // Assert: Ensure the data survived the round trip to the disk and back
        assertTrue(loadedResult.isPresent());
        ReadOnlyMediTrack loadedData = loadedResult.get();
        assertEquals(1, loadedData.getPersonnelList().size());
        assertEquals("Test Soldier", loadedData.getPersonnelList().get(0).getName());
    }

    @Test
    void test_saveAndRead_supplyData_roundTripsCorrectly() throws Exception {
        // Arrange
        MediTrack model = new MediTrack();
        model.addSupplyRecord(new Supply("Aspirin", 200, LocalDate.of(2028, 6, 15)));
        model.addSupplyRecord(new Supply("Morphine", 50, LocalDate.of(2027, 12, 1)));

        // Act
        storageManager.saveMediTrackData(model);
        Optional<ReadOnlyMediTrack> loaded = storageManager.readMediTrackData();

        // Assert
        assertTrue(loaded.isPresent());
        assertEquals(2, loaded.get().getSupplyList().size());
        assertEquals("Aspirin", loaded.get().getSupplyList().get(0).getName());
    }

    @Test
    void test_saveAndRead_personnelWithBloodGroupAndAllergies_roundTripsCorrectly() throws Exception {
        // Arrange
        MediTrack model = new MediTrack();
        Personnel p = new Personnel("Jane", Status.MC, BloodGroup.AB_POS, "Penicillin");
        p.setStatusExpiryDate(LocalDate.of(2026, 7, 1));
        model.addPersonnelRecord(p);

        // Act
        storageManager.saveMediTrackData(model);
        Optional<ReadOnlyMediTrack> loaded = storageManager.readMediTrackData();

        // Assert
        assertTrue(loaded.isPresent());
        Personnel loadedPerson = loaded.get().getPersonnelList().get(0);
        assertEquals("Jane", loadedPerson.getName());
        assertEquals(Status.MC, loadedPerson.getStatus());
        assertEquals(BloodGroup.AB_POS, loadedPerson.getBloodGroup());
        assertEquals("Penicillin", loadedPerson.getAllergies());
        assertEquals(LocalDate.of(2026, 7, 1), loadedPerson.getStatusExpiryDate());
    }

    @Test
    void test_saveAndRead_dutySlotData_roundTripsCorrectly() throws Exception {
        // Arrange
        MediTrack model = new MediTrack();
        DutySlot slot = new DutySlot(LocalDate.of(2026, 5, 1),
                LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");
        model.addDutySlotRecord(slot);

        // Act
        storageManager.saveMediTrackData(model);
        Optional<ReadOnlyMediTrack> loaded = storageManager.readMediTrackData();

        // Assert
        assertTrue(loaded.isPresent());
        assertEquals(1, loaded.get().getDutySlots().size());
        assertEquals("Alice", loaded.get().getDutySlots().get(0).getPersonnelName());
    }

    @Test
    void test_saveAndRead_emptyModel_roundTripsCorrectly() throws Exception {
        // Arrange
        MediTrack emptyModel = new MediTrack();

        // Act
        storageManager.saveMediTrackData(emptyModel);
        Optional<ReadOnlyMediTrack> loaded = storageManager.readMediTrackData();

        // Assert
        assertTrue(loaded.isPresent());
        assertTrue(loaded.get().getSupplyList().isEmpty());
        assertTrue(loaded.get().getPersonnelList().isEmpty());
        assertTrue(loaded.get().getDutySlots().isEmpty());
    }

    @Test
    void test_readMediTrackData_corruptPersonnelRecord_skipsAndLoadsRest() throws Exception {
        // Arrange: manually write JSON with one corrupt personnel (invalid status)
        Path tempFile = testFolder.resolve("corrupt_data.json");
        JsonMediTrackStorage jsonStorage = new JsonMediTrackStorage(tempFile);
        StorageManager corruptStorageManager = new StorageManager(jsonStorage);

        String corruptJson = "{\"supplies\":["
                + "{\"name\":\"Bandages\",\"quantity\":100,\"expiryDate\":\"2028-01-01\"}"
                + "],\"personnel\":["
                + "{\"name\":\"Good\",\"status\":\"FIT\",\"bloodGroup\":null,"
                + "\"allergies\":\"\",\"lastModified\":\"2026-01-01T10:00:00\",\"statusExpiryDate\":null},"
                + "{\"name\":\"Bad\",\"status\":\"INVALID_STATUS\",\"bloodGroup\":null,"
                + "\"allergies\":\"\",\"lastModified\":\"2026-01-01T10:00:00\",\"statusExpiryDate\":null}"
                + "],\"dutySlots\":[]}";
        java.nio.file.Files.writeString(tempFile, corruptJson);

        // Act
        Optional<ReadOnlyMediTrack> loaded = corruptStorageManager.readMediTrackData();

        // Assert: corrupt record skipped, good record preserved
        assertTrue(loaded.isPresent());
        assertEquals(1, loaded.get().getPersonnelList().size());
        assertEquals("Good", loaded.get().getPersonnelList().get(0).getName());
        assertEquals(1, loaded.get().getSupplyList().size());
    }
}
