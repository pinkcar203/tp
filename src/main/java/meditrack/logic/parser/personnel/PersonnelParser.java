package meditrack.logic.parser.personnel;

import meditrack.logic.commands.personnel.*;
import meditrack.logic.parser.exceptions.*;

import meditrack.model.ModelManager;
import meditrack.model.Status;

/**
 * Parses raw strings into personnel {@link meditrack.logic.commands.Command}s
 * (used by modals that build prefix-style argument strings).
 */
public class PersonnelParser {

    private static final String STATUS_VALID = "FIT, LIGHT_DUTY, MC, CASUALTY, PENDING";

    /**
     * Expected format: {@code add_personnel n/NAME s/STATUS}
     *
     * @param args text after the command word
     * @return command ready to execute
     * @throws ParseException if name is blank or status is invalid
     */
    public static AddPersonnelCommand parseAddPersonnel(String args) throws ParseException {
        String name = extractPrefixValue(args, "n/");
        String statusRaw = extractPrefixValue(args, "s/");

        if (name == null || name.isBlank()) {
            throw new ParseException("Name must not be blank. Format: add_personnel n/NAME s/STATUS");
        }

        Status status = parseStatus(statusRaw);
        return new AddPersonnelCommand(name.trim(), status);
    }

    /**
     * Expected format: {@code remove_personnel INDEX}
     *
     * @param args text after the command word
     * @return command ready to execute
     * @throws ParseException if the index is not a positive integer
     */
    public static RemovePersonnelCommand parseRemovePersonnel(String args) throws ParseException {
        int index = parsePositiveInt(args.trim(),
                "Index must be a positive integer. Format: remove_personnel INDEX");
        return new RemovePersonnelCommand(index);
    }

    /**
     * Expected format: {@code update_status INDEX s/STATUS}
     */
    public static UpdateStatusCommand parseUpdateStatus(String args) throws ParseException {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            throw new ParseException("Format: update_status INDEX s/STATUS");
        }

        int index = parsePositiveInt(parts[0],
                "Index must be a positive integer. Format: update_status INDEX s/STATUS");

        String statusRaw = extractPrefixValue(parts[1], "s/");
        Status status = parseStatus(statusRaw);

        return new UpdateStatusCommand(index, status);
    }

    /**
     * No arguments; FIT list is validated when the command runs.
     *
     * @return command ready to execute
     */
    public static GenerateRosterCommand parseGenerateRoster() {
        return new GenerateRosterCommand();
    }

    /**
     * Returns the value after {@code prefix}, or {@code null} if missing.
     */
    private static String extractPrefixValue(String args, String prefix) {
        if (args == null) {
            return null;
        }
        int start = args.indexOf(prefix);
        if (start == -1) {
            return null;
        }
        start += prefix.length();
        int end = args.length();
        for (int i = start + 1; i < args.length() - 1; i++) {
            if (args.charAt(i + 1) == '/' && Character.isLetter(args.charAt(i))) {
                end = i;
                break;
            }
        }
        return args.substring(start, end).trim();
    }

    private static Status parseStatus(String raw) throws ParseException {
        if (raw == null || raw.isBlank()) {
            throw new ParseException(
                    "Status must not be blank. Valid values: " + STATUS_VALID);
        }
        try {
            return Status.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage() + " Valid values: " + STATUS_VALID);
        }
    }

    private static int parsePositiveInt(String token, String errorMessage) throws ParseException {
        try {
            int value = Integer.parseInt(token);
            if (value < 1) {
                throw new ParseException(errorMessage);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ParseException(errorMessage);
        }
    }
}
