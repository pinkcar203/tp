package meditrack.logic.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import meditrack.logic.parser.exceptions.ParseException;
import meditrack.model.Model;

/** Checks UI fields before a command is built. */
public class Parser {

    private final Model model;

    /**
     * @param model used for list size and duplicate checks during validation
     */
    public Parser(Model model) {
        this.model = model;
    }

    /**
     * Validates fields for commandType.
     *
     * @throws ParseException if any rule fails
     */
    public void validate(CommandType commandType, Map<String, String> fields) throws ParseException {
        switch (commandType) {
        case ADD_SUPPLY:
            validateSupplyFields(fields);
            validateNoDuplicateSupplyName(fields.get("name"));
            break;

        case EDIT_SUPPLY:
            validateSupplyFields(fields);
            validateIndexField(fields, model.getFilteredSupplyList().size());
            break;

        case DELETE_SUPPLY:
            validateIndexField(fields, model.getFilteredSupplyList().size());
            break;

        case GENERATE_RESUPPLY_REPORT:
            if (model.getFilteredSupplyList().isEmpty()) {
                throw new ParseException("Cannot generate resupply report: no supply records exist.");
            }
            break;

        default:
            break;
        }
    }

    private void validateSupplyFields(Map<String, String> fields) throws ParseException {
        String name = fields.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ParseException("Name: Supply name must not be empty.");
        }

        String qtyStr = fields.get("qty");
        if (qtyStr == null || qtyStr.trim().isEmpty()) {
            throw new ParseException("Quantity: Quantity must not be empty.");
        }
        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0) {
                throw new ParseException("Quantity: Quantity must be a positive integer greater than 0.");
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Quantity: Quantity must be a valid integer.");
        }

        String expiryStr = fields.get("expiry");
        if (expiryStr == null || expiryStr.trim().isEmpty()) {
            throw new ParseException("Expiry Date: Expiry date must not be empty.");
        }
        try {
            LocalDate expiry = LocalDate.parse(expiryStr.trim());
            if (!expiry.isAfter(LocalDate.now())) {
                throw new ParseException("Expiry Date: Expiry date must be a future date.");
            }
        } catch (DateTimeParseException e) {
            throw new ParseException("Expiry Date: Expiry date must be a valid date in YYYY-MM-DD format.");
        }
    }

    private void validateNoDuplicateSupplyName(String name) throws ParseException {
        boolean duplicate = model.getFilteredSupplyList().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(name.trim()));
        if (duplicate) {
            throw new ParseException("Name: A supply with this name already exists.");
        }
    }

    private void validateIndexField(Map<String, String> fields, int listSize) throws ParseException {
        String indexStr = fields.get("index");
        if (indexStr == null || indexStr.trim().isEmpty()) {
            throw new ParseException("Index: Index must not be empty.");
        }
        try {
            int index = Integer.parseInt(indexStr.trim());
            if (index <= 0) {
                throw new ParseException("Index: Index must be a positive integer.");
            }
            if (index > listSize) {
                throw new ParseException("Index: Index is out of bounds. List has " + listSize + " item(s).");
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Index: Index must be a valid integer.");
        }
    }
}
