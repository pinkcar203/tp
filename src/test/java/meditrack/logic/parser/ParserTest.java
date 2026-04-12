package meditrack.logic.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.logic.parser.exceptions.ParseException;
import meditrack.model.ModelManager;
import meditrack.model.Supply;

/**
 * Tests for the Parser component.
 * Verifies that user input parameters are correctly validated before generating commands.
 */
class ParserTest {

    private ModelManager model;
    private Parser parser;

    /**
     * Initializes a fresh ModelManager and Parser before each test to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        model = new ModelManager();
        parser = new Parser(model);
    }

    // --- ADD SUPPLY VALIDATION TESTS ---

    /**
     * Tests that providing valid parameters for adding a supply passes validation without throwing.
     */
    @Test
    void validate_addSupply_validFields_success() {
        assertDoesNotThrow(() -> parser.validate(CommandType.ADD_SUPPLY, Map.of(
                "name", "Bandages",
                "qty", "100",
                "expiry", LocalDate.now().plusDays(60).toString()
        )));
    }

    /**
     * Tests that attempting to add a supply with an empty or whitespace-only name throws a ParseException.
     */
    @Test
    void validate_addSupply_emptyName_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.ADD_SUPPLY, Map.of(
                        "name", "   ", // Testing whitespace rejection
                        "qty", "100",
                        "expiry", LocalDate.now().plusDays(60).toString()
                )));
        assertTrue(ex.getMessage().contains("Name"));
    }

    /**
     * Tests that attempting to add a supply with zero quantity throws a ParseException.
     */
    @Test
    void validate_addSupply_zeroQuantity_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.ADD_SUPPLY, Map.of(
                        "name", "Bandages",
                        "qty", "0",
                        "expiry", LocalDate.now().plusDays(60).toString()
                )));
        assertTrue(ex.getMessage().contains("Quantity"));
    }

    /**
     * Tests that attempting to add a supply with a negative quantity throws a ParseException.
     */
    @Test
    void validate_addSupply_negativeQuantity_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.ADD_SUPPLY, Map.of(
                        "name", "Bandages",
                        "qty", "-5",
                        "expiry", LocalDate.now().plusDays(60).toString()
                )));
        assertTrue(ex.getMessage().contains("Quantity"));
    }

    /**
     * Tests that attempting to add a supply with non-numeric quantity characters throws a ParseException.
     */
    @Test
    void validate_addSupply_nonNumericQuantity_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.ADD_SUPPLY, Map.of(
                        "name", "Bandages",
                        "qty", "abc",
                        "expiry", LocalDate.now().plusDays(60).toString()
                )));
        assertTrue(ex.getMessage().contains("Quantity"));
    }

    /**
     * Tests that attempting to add a supply with an expiration date in the past throws a ParseException.
     */
    @Test
    void validate_addSupply_pastExpiryDate_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.ADD_SUPPLY, Map.of(
                        "name", "Bandages",
                        "qty", "100",
                        "expiry", LocalDate.now().minusDays(1).toString()
                )));
        assertTrue(ex.getMessage().contains("Expiry Date"));
    }

    /**
     * Tests that providing a malformed date string throws a ParseException.
     */
    @Test
    void validate_addSupply_invalidDateFormat_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.ADD_SUPPLY, Map.of(
                        "name", "Bandages",
                        "qty", "100",
                        "expiry", "not-a-date"
                )));
        assertTrue(ex.getMessage().contains("Expiry Date"));
    }

    // --- EDIT SUPPLY VALIDATION TESTS ---

    /**
     * Tests that providing valid parameters for editing a supply passes validation.
     */
    @Test
    void validate_editSupply_validFields_success() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        assertDoesNotThrow(() -> parser.validate(CommandType.EDIT_SUPPLY, Map.of(
                "name", "Bandages XL",
                "qty", "200",
                "expiry", LocalDate.now().plusDays(60).toString(),
                "index", "1"
        )));
    }

    /**
     * Tests that providing a target edit index greater than the list size throws a ParseException.
     */
    @Test
    void validate_editSupply_indexOutOfBounds_throwsParseException() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.EDIT_SUPPLY, Map.of(
                        "name", "Bandages XL",
                        "qty", "200",
                        "expiry", LocalDate.now().plusDays(60).toString(),
                        "index", "5"
                )));
        assertTrue(ex.getMessage().contains("Index"));
    }

    /**
     * Tests that attempting to edit a supply to have a negative quantity throws a ParseException.
     */
    @Test
    void validate_editSupply_negativeQuantity_throwsParseException() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.EDIT_SUPPLY, Map.of(
                        "name", "Bandages XL",
                        "qty", "-10",
                        "expiry", LocalDate.now().plusDays(60).toString(),
                        "index", "1"
                )));
        assertTrue(ex.getMessage().contains("Quantity"));
    }

    // --- DELETE SUPPLY VALIDATION TESTS ---

    /**
     * Tests that providing a valid index for deletion passes validation.
     */
    @Test
    void validate_deleteSupply_validIndex_success() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        assertDoesNotThrow(() -> parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                "index", "1"
        )));
    }

    /**
     * Tests that a zero-based index for a 1-based index system throws a ParseException.
     */
    @Test
    void validate_deleteSupply_zeroIndex_throwsParseException() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                        "index", "0"
                )));
        assertTrue(ex.getMessage().contains("Index"));
    }

    /**
     * Tests that a non-numeric index format throws a ParseException.
     */
    @Test
    void validate_deleteSupply_nonNumericIndex_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                        "index", "abc"
                )));
        assertTrue(ex.getMessage().contains("Index"));
    }

    /**
     * Tests that providing a target deletion index greater than the list size throws a ParseException.
     */
    @Test
    void validate_deleteSupply_indexOutOfBounds_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                        "index", "1"
                )));
        assertTrue(ex.getMessage().contains("Index"));
    }

    // --- REPORT GENERATION VALIDATION TESTS ---

    /**
     * Tests that generating a report passes validation when supplies exist.
     */
    @Test
    void validate_generateResupplyReport_suppliesExist_success() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        assertDoesNotThrow(() -> parser.validate(
                CommandType.GENERATE_RESUPPLY_REPORT, Map.of()));
    }

    /**
     * Tests that generating a report when the inventory is empty throws a ParseException.
     */
    @Test
    void validate_generateResupplyReport_noSupplies_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.GENERATE_RESUPPLY_REPORT, Map.of()));
        assertTrue(ex.getMessage().contains("no supply records"));
    }
}