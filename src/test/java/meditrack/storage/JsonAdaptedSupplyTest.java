package meditrack.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Supply;

/*
 * Equivalence Partitions:
 *
 * toModelType() 
 * Parameter: name
 *   Valid:   non-blank string ("Bandages"), string with spaces ("  Gauze  " )
 *   Invalid: null, blank ("   ")
 *
 * Parameter: quantity
 *   Valid:   any integer (positive, zero, negative  no validation here)
 *
 * Parameter: expiryDate
 *   Valid:   ISO-8601 date string ("2027-12-31")
 *   Invalid: null, blank (""), malformed ("31-12-2027")
 */

// Verification tests
public class JsonAdaptedSupplyTest {

    private static final String VALID_NAME = "Bandages";
    private static final int VALID_QUANTITY = 50;
    private static final String VALID_EXPIRY = "2027-12-31";

    @Test
    public void toModelType_validSupplyDetails_returnsSupply() throws CommandException {
        JsonAdaptedSupply supplyJson = new JsonAdaptedSupply(VALID_NAME, VALID_QUANTITY, VALID_EXPIRY);
        Supply supply = supplyJson.toModelType();

        assertEquals(VALID_NAME, supply.getName());
        assertEquals(VALID_QUANTITY, supply.getQuantity());
        assertEquals(LocalDate.parse(VALID_EXPIRY), supply.getExpiryDate());
    }

    @Test
    public void toModelType_nullName_throwsCommandException() {
        JsonAdaptedSupply supplyJson = new JsonAdaptedSupply(null, VALID_QUANTITY, VALID_EXPIRY);
        assertThrows(CommandException.class, supplyJson::toModelType);
    }

    @Test
    public void toModelType_invalidDatePattern_throwsCommandException() {
        JsonAdaptedSupply supplyJson = new JsonAdaptedSupply(VALID_NAME, VALID_QUANTITY, "31-12-2027");
        assertThrows(CommandException.class, supplyJson::toModelType);
    }

    @Test
    public void fromModelType_validSupply_success() {
        Supply s = new Supply("Aspirin", 100, LocalDate.of(2028, 1, 1));
        assertDoesNotThrow(() -> JsonAdaptedSupply.fromModelType(s));
    }

    @Test
    void test_toModelType_blankName_throwsCommandException() {
        // Arrange
        String blankName = "   ";
        JsonAdaptedSupply supply = new JsonAdaptedSupply(blankName, 50, "2027-12-31");

        // Act & Assert
        assertThrows(CommandException.class, supply::toModelType);
    }

    @Test
    void test_toModelType_nullExpiryDate_throwsCommandException() {
        // Arrange
        String nullExpiry = null;
        JsonAdaptedSupply supply = new JsonAdaptedSupply("Bandages", 50, nullExpiry);

        // Act & Assert
        assertThrows(CommandException.class, supply::toModelType);
    }

    @Test
    void test_toModelType_blankExpiryDate_throwsCommandException() {
        // Arrange
        String blankExpiry = "  ";
        JsonAdaptedSupply supply = new JsonAdaptedSupply("Bandages", 50, blankExpiry);

        // Act & Assert
        assertThrows(CommandException.class, supply::toModelType);
    }

    @Test
    void test_toModelType_validWithTrimming_returnsCorrectSupply() throws CommandException {
        // Arrange
        String nameWithSpaces = "  Gauze  ";
        JsonAdaptedSupply supply = new JsonAdaptedSupply(nameWithSpaces, 25, "2028-06-15");

        // Act
        Supply result = supply.toModelType();

        // Assert
        assertEquals("Gauze", result.getName());
        assertEquals(25, result.getQuantity());
    }

    @Test
    void test_toModelType_zeroQuantity_returnsSupply() throws CommandException {
        // Arrange
        int zeroQuantity = 0;
        JsonAdaptedSupply supply = new JsonAdaptedSupply("Test", zeroQuantity, "2028-01-01");

        // Act
        Supply result = supply.toModelType();

        // Assert
        assertEquals(zeroQuantity, result.getQuantity());
    }
}
