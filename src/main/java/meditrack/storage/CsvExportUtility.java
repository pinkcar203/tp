package meditrack.storage;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import meditrack.commons.core.Constants;
import meditrack.model.DutySlot;
import meditrack.model.Personnel;
import meditrack.model.ReadOnlyMediTrack;
import meditrack.model.Role;
import meditrack.model.Status;
import meditrack.model.Supply;

/**
 * Writes role-filtered CSV snapshots under {@code exports/} — each role only sees rows they are meant to
 * (same rules as in the app). Uses {@link Constants} for low-stock / expiring thresholds so we do not
 * hard-code random cutoffs here.
 */
public class CsvExportUtility {

    /**
     * Same as overload, but default folder {@code ./exports} under the working directory.
     */
    public static Path exportData(ReadOnlyMediTrack data, Role currentRole) throws IOException {
        Path defaultExportDir = Paths.get(System.getProperty("user.dir"), "exports");
        return exportData(data, currentRole, defaultExportDir);
    }

    /**
     * @return path to the file we just wrote (filename includes role + timestamp)
     */
    public static Path exportData(ReadOnlyMediTrack data, Role currentRole, Path exportDir) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = currentRole.name() + "_Export_" + timestamp + ".csv";

        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        Path filePath = exportDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {

            // - Export Personnel Roster (MO, PC, and Field Medic ONLY) -
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

            // - Export Duty Roster (Platoon Commander ONLY) -
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

            // - Export Supply Inventory (Logistics Officer and Field Medic ONLY) -
            if (currentRole == Role.LOGISTICS_OFFICER || currentRole == Role.FIELD_MEDIC) {
                writer.append("=== SUPPLY INVENTORY ===\n");
                writer.append("Item Name,Quantity,Expiry Date,Action Required\n");

                LocalDate expiryCutoff = LocalDate.now().plusDays(Constants.EXPIRY_THRESHOLD_DAYS);

                for (Supply s : data.getSupplyList()) {
                    String flag = "";
                    if (s.getQuantity() < Constants.LOW_STOCK_THRESHOLD_QUANTITY
                            || s.getExpiryDate().isBefore(expiryCutoff)) {
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