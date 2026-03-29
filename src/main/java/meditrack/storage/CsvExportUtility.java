package meditrack.storage;

import meditrack.model.DutySlot;
import meditrack.model.Personnel;
import meditrack.model.ReadOnlyMediTrack;
import meditrack.model.Role;
import meditrack.model.Status;
import meditrack.model.Supply;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to export MediTrack data to a universally readable CSV format.
 * Implements Role-Based Access Control (RBAC) to protect medical confidentiality.
 */
public class CsvExportUtility {

    /**
     * Exports the application data to a CSV file based on the user's security clearance.
     *
     * @param data The current read-only state of the application data.
     * @param currentRole The role of the user requesting the export.
     * @return The file path where the CSV was saved.
     * @throws IOException If there is an error writing to the file system.
     */
    public static Path exportData(ReadOnlyMediTrack data, Role currentRole) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = currentRole.name() + "_Export_" + timestamp + ".csv";
        Path exportDir = Paths.get(System.getProperty("user.dir"), "exports");

        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        Path filePath = exportDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {

            // --- Export Personnel Roster (MO, PC, and Field Medic ONLY) ---
            if (currentRole == Role.MEDICAL_OFFICER || currentRole == Role.FIELD_MEDIC || currentRole == Role.PLATOON_COMMANDER) {
                writer.append("=== PERSONNEL ROSTER ===\n");
                writer.append("Name,Status,Action Required\n");

                for (Personnel p : data.getPersonnelList()) {
                    Status s = p.getStatus();
                    String flag = "";
                    if (s == Status.PENDING || s == Status.CASUALTY || s == Status.MC || s == Status.LIGHT_DUTY) {
                        flag = "⚠ MEDICAL ATTENTION";
                    }
                    writer.append(String.format("\"%s\",\"%s\",\"%s\"\n", p.getName(), s.toString(), flag));
                }
                writer.append("\n");
            }

            // Export Duty Roster (Platoon Commander ONLY) 
            if (currentRole == Role.PLATOON_COMMANDER) {
                writer.append("=== DUTY ROSTER ===\n");
                writer.append("Time Slot,Duty Type,Personnel\n");

                for (DutySlot slot : data.getDutySlots()) {
                    writer.append(String.format("\"%s\",\"%s\",\"%s\"\n",
                            slot.getTimeSlotDisplay(),
                            slot.getDutyType().toString(),
                            slot.getPersonnelName()));
                }
                writer.append("\n");
            }

            // Export Supply Inventory (Logistics Officer and Field Medic ONLY) 
            if (currentRole == Role.LOGISTICS_OFFICER || currentRole == Role.FIELD_MEDIC) {
                writer.append("=== SUPPLY INVENTORY ===\n");
                writer.append("Item Name,Quantity,Expiry Date,Action Required\n");

                LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);

                for (Supply s : data.getSupplyList()) {
                    String flag = "";
                    if (s.getQuantity() < 20 || s.getExpiryDate().isBefore(thirtyDaysFromNow)) {
                        flag = "⚠ LOW / EXPIRING";
                    }
                    writer.append(String.format("\"%s\",%d,\"%s\",\"%s\"\n",
                            s.getName(),
                            s.getQuantity(),
                            s.getExpiryDate().toString(),
                            flag));
                }
            }

            writer.flush();
        }
        return filePath;
    }
}