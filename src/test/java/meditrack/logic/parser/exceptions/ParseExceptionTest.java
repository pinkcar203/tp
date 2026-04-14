package meditrack.logic.parser.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;


class ParseExceptionTest {

    @Test
    void test_constructor_messageOnly_storesMessage() {
        // Arrange
        String expectedMessage = "Parse error occurred";

        // Act
        ParseException ex = new ParseException(expectedMessage);

        // Assert
        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void test_constructor_messageAndCause_storesBoth() {
        // Arrange
        String expectedMessage = "Wrapped parse error";
        Throwable expectedCause = new NumberFormatException("bad number");

        // Act
        ParseException ex = new ParseException(expectedMessage, expectedCause);

        // Assert
        assertEquals(expectedMessage, ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("bad number", ex.getCause().getMessage());
    }
}
