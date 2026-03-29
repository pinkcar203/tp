package meditrack.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A class to access MediTrack data stored as a JSON file on the hard drive.
 * Handles the low-level File I/O operations using the Jackson library.
 */
public class JsonMediTrackStorage {

    private final Path filePath = Paths.get("data.json");
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieves the file path where the application data is stored.
     *
     * @return The Path object pointing to the data.json file.
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Reads the serialized data from the JSON file.
     *
     * @return An Optional containing the JsonSerializableMediTrack object if successful, or an empty Optional if the file does not exist or cannot be read.
     */
    public Optional<JsonSerializableMediTrack> readData() {
        File file = filePath.toFile();
        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            JsonSerializableMediTrack data = objectMapper.readValue(file, JsonSerializableMediTrack.class);
            return Optional.of(data);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Saves the serialized data to the JSON file.
     *
     * @param data The JsonSerializableMediTrack object containing all application data to be saved.
     * @throws IOException If there is an issue writing the file to the disk.
     */
    public void saveData(JsonSerializableMediTrack data) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), data);
    }

    /**
     * Saves data to a specific path (used for atomic writes via temp files).
     *
     * @param data       The data to serialize.
     * @param targetPath The file path to write to.
     * @throws IOException If there is an issue writing the file.
     */
    public void saveDataToPath(JsonSerializableMediTrack data, Path targetPath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetPath.toFile(), data);
    }
}