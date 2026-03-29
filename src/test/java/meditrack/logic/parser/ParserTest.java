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

class ParserTest {

    private ModelManager model;
    private Parser parser;

    @BeforeEach
    void setUp() {
        model = new ModelManager();
        parser = new Parser(model);
    }

    @Test
    void validate_addSupply_validFields_success() {
        assertDoesNotThrow(() -> parser.validate(CommandType.ADD_SUPPLY, Map.of(
                "name", "Bandages",
                "qty", "100",
                "expiry", LocalDate.now().plusDays(60).toString()
        )));
    }

    @Test
    void validate_addSupply_emptyName_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.ADD_SUPPLY, Map.of(
                        "name", "",
                        "qty", "100",
                        "expiry", LocalDate.now().plusDays(60).toString()
                )));
        assertTrue(ex.getMessage().contains("Name"));
    }

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

    @Test
    void validate_deleteSupply_validIndex_success() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        assertDoesNotThrow(() -> parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                "index", "1"
        )));
    }

    @Test
    void validate_deleteSupply_zeroIndex_throwsParseException() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                        "index", "0"
                )));
        assertTrue(ex.getMessage().contains("Index"));
    }

    @Test
    void validate_deleteSupply_nonNumericIndex_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                        "index", "abc"
                )));
        assertTrue(ex.getMessage().contains("Index"));
    }

    @Test
    void validate_deleteSupply_indexOutOfBounds_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.DELETE_SUPPLY, Map.of(
                        "index", "1"
                )));
        assertTrue(ex.getMessage().contains("Index"));
    }

    @Test
    void validate_generateResupplyReport_suppliesExist_success() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        assertDoesNotThrow(() -> parser.validate(
                CommandType.GENERATE_RESUPPLY_REPORT, Map.of()));
    }

    @Test
    void validate_generateResupplyReport_noSupplies_throwsParseException() {
        ParseException ex = assertThrows(ParseException.class, () ->
                parser.validate(CommandType.GENERATE_RESUPPLY_REPORT, Map.of()));
        assertTrue(ex.getMessage().contains("no supply records"));
    }
}
