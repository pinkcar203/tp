package meditrack.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;



class JsonMediTrackStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void test_readData_fileDoesNotExist_returnsEmptyOptional() {
        // Arrange
        Path nonExistentPath = tempDir.resolve("nonexistent.json");
        JsonMediTrackStorage storage = new JsonMediTrackStorage(nonExistentPath);

        // Act
        Optional<JsonSerializableMediTrack> result = storage.readData();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void test_readData_fileExistsWithValidJson_returnsPopulatedOptional() throws IOException {
        // Arrange
        Path validFile = tempDir.resolve("valid.json");
        String validJson = "{\"supplies\":[],\"personnel\":[],\"dutySlots\":[]}";
        Files.writeString(validFile, validJson);
        JsonMediTrackStorage storage = new JsonMediTrackStorage(validFile);

        // Act
        Optional<JsonSerializableMediTrack> result = storage.readData();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().supplies.size());
    }

    @Test
    void test_readData_fileExistsWithMalformedJson_returnsEmptyOptional() throws IOException {
        // Arrange
        Path corruptFile = tempDir.resolve("corrupt.json");
        String malformedJson = "{not valid json content!!!";
        Files.writeString(corruptFile, malformedJson);
        JsonMediTrackStorage storage = new JsonMediTrackStorage(corruptFile);

        // Act
        Optional<JsonSerializableMediTrack> result = storage.readData();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void test_saveData_emptyData_createsValidJsonFile() throws IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.json");
        JsonMediTrackStorage storage = new JsonMediTrackStorage(outputFile);
        JsonSerializableMediTrack emptyData = new JsonSerializableMediTrack(
                List.of(), List.of(), List.of());

        // Act
        storage.saveData(emptyData);

        // Assert
        assertTrue(Files.exists(outputFile));
        String content = Files.readString(outputFile);
        assertTrue(content.contains("supplies"));
    }

    @Test
    void test_saveAndReadRoundTrip_populatedData_preservesContent() throws IOException {
        // Arrange
        Path roundTripFile = tempDir.resolve("roundtrip.json");
        JsonMediTrackStorage storage = new JsonMediTrackStorage(roundTripFile);
        JsonAdaptedSupply supply = new JsonAdaptedSupply("Bandages", 100, "2027-12-31");
        JsonSerializableMediTrack dataToSave = new JsonSerializableMediTrack(
                List.of(supply), List.of(), List.of());

        // Act
        storage.saveData(dataToSave);
        Optional<JsonSerializableMediTrack> loadedData = storage.readData();

        // Assert
        assertTrue(loadedData.isPresent());
        assertEquals(1, loadedData.get().supplies.size());
        assertEquals("Bandages", loadedData.get().supplies.get(0).name);
    }

    @Test
    void test_getFilePath_injectedPath_returnsCorrectPath() {
        // Arrange
        Path customPath = tempDir.resolve("custom.json");
        JsonMediTrackStorage storage = new JsonMediTrackStorage(customPath);

        // Act & Assert
        assertEquals(customPath, storage.getFilePath());
    }

    @Test
    void test_constructor_defaultPath_usesDataJson() {
        // Arrange & Act
        JsonMediTrackStorage storage = new JsonMediTrackStorage();

        // Assert
        assertEquals("data.json", storage.getFilePath().toString());
    }
}
