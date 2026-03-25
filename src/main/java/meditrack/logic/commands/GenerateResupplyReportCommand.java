package meditrack.logic.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.model.Supply;

/**
 * Generates a resupply report flagging items that are low stock or
 * expiring soon. Only the Logistics Officer can run this.
 */
public class GenerateResupplyReportCommand extends Command {

    public static final String MESSAGE_ALL_CLEAR = "All supplies are adequately stocked and within expiry dates.";
    public static final String MESSAGE_HEADER = "Resupply Report — %d item(s) flagged:\n";

    private final int quantityThreshold;
    private final int daysThreshold;

    /**
     * @param quantityThreshold counts as low stock below this quantity
     * @param daysThreshold expiring within this many days (from today) is flagged
     */
    public GenerateResupplyReportCommand(int quantityThreshold, int daysThreshold) {
        this.quantityThreshold = quantityThreshold;
        this.daysThreshold = daysThreshold;
    }

    /** Shared helper so the UI table uses the same rules as the command. */
    public static List<ReportEntry> collectFlaggedEntries(Model model, int quantityThreshold,
            int daysThreshold) {
        List<Supply> lowStock = model.getLowStockSupplies(quantityThreshold);
        List<Supply> expiring = model.getExpiringSupplies(daysThreshold);

        Set<String> lowStockNames = new LinkedHashSet<>();
        for (Supply s : lowStock) {
            lowStockNames.add(s.getName().toLowerCase());
        }
        Set<String> expiringNames = new LinkedHashSet<>();
        for (Supply s : expiring) {
            expiringNames.add(s.getName().toLowerCase());
        }

        List<ReportEntry> entries = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Supply s : lowStock) {
            String key = s.getName().toLowerCase();
            if (seen.add(key)) {
                boolean alsoExpiring = expiringNames.contains(key);
                String reason = alsoExpiring ? "Both" : "Low Stock";
                entries.add(new ReportEntry(s, reason));
            }
        }
        for (Supply s : expiring) {
            String key = s.getName().toLowerCase();
            if (seen.add(key)) {
                entries.add(new ReportEntry(s, "Expiring Soon"));
            }
        }
        return Collections.unmodifiableList(entries);
    }

    /** Builds the text report from flagged supplies. */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<ReportEntry> entries = collectFlaggedEntries(model, quantityThreshold, daysThreshold);

        if (entries.isEmpty()) {
            return new CommandResult(MESSAGE_ALL_CLEAR);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(MESSAGE_HEADER, entries.size()));
        for (ReportEntry entry : entries) {
            sb.append(String.format("  - %s | Qty: %d | Expiry: %s | Reason: %s%n",
                    entry.supply.getName(),
                    entry.supply.getQuantity(),
                    entry.supply.getExpiryDate(),
                    entry.reason));
        }
        return new CommandResult(sb.toString().trim());
    }

    /** Logistics officer only. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.LOGISTICS_OFFICER);
    }

    /** One flagged row for the report (supply + reason text). */
    public static class ReportEntry {
        private final Supply supply;
        private final String reason;

        /** @param reason e.g. Low Stock, Expiring Soon, Both */
        public ReportEntry(Supply supply, String reason) {
            this.supply = supply;
            this.reason = reason;
        }

        /** Returns the flagged supply. */
        public Supply getSupply() {
            return supply;
        }

        /** Returns the flag reason string. */
        public String getReason() {
            return reason;
        }
    }
}
