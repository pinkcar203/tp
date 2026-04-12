package meditrack.logic.commands.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/*
 * Equivalence Partitions:
 *
 *  constructor(message) 
 * Parameter: message
 *   Valid:   non-null string
 *
 *  constructor(message, cause) 
 * Parameter: message
 *   Valid:   non-null string
 *
 * Parameter: cause
 *   Valid:   non-null Throwable
 */

// Verification tests
class CommandExceptionTest {

    @Test
    void test_constructor_messageOnly_storesMessage() {
        // Arrange
        String expectedMessage = "Test error message";

        // Act
        CommandException ex = new CommandException(expectedMessage);

        // Assert
        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void test_constructor_messageAndCause_storesBoth() {
        // Arrange
        String expectedMessage = "Wrapped error";
        Throwable expectedCause = new RuntimeException("root cause");

        // Act
        CommandException ex = new CommandException(expectedMessage, expectedCause);

        // Assert
        assertEquals(expectedMessage, ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("root cause", ex.getCause().getMessage());
    }
}
