package meditrack.storage;

import meditrack.model.Personnel;
import meditrack.model.ReadOnlyMediTrack;
import meditrack.model.Supply;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to export MediTrack data to a universally readable CSV format.
 */
public class CsvExportUtility {

    /**
     * Exports the current Personnel Roster and Supply Inventory to a CSV file.
     * * @param data The current read-only state of the application data.
     * @return The file path where the CSV was saved.
     * @throws IOException If there is an error writing to the file system.
     */
    public static Path exportData(ReadOnlyMediTrack data) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "MediTrack_Export_" + timestamp + ".csv";
        Path exportDir = Paths.get(System.getProperty("user.dir"), "exports");

        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        Path filePath = exportDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {

            // --- Export Personnel Roster ---
            writer.append("=== PERSONNEL ROSTER ===\n");
            writer.append("Name,Status\n");
            for (Personnel p : data.getPersonnelList()) {
                writer.append(String.format("\"%s\",\"%s\"\n", p.getName(), p.getStatus().toString()));
            }

            writer.append("\n");

            // --- Export Supply Inventory ---
            writer.append("=== SUPPLY INVENTORY ===\n");
            writer.append("Item Name,Quantity,Expiry Date\n");
            for (Supply s : data.getSupplyList()) {
                writer.append(String.format("\"%s\",%d,\"%s\"\n",
                        s.getName(),
                        s.getQuantity(),
                        s.getExpiryDate().toString()));
            }

            writer.flush();
        }
        return filePath;
    }
}