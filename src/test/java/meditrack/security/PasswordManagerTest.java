package meditrack.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the PasswordManager, ensuring cryptographic hashing and verification function correctly.
 */
public class PasswordManagerTest {

    @Test
    public void hashPassword_validInput_returnsValidBcryptHash() {
        // Act
        String hash = PasswordManager.hashPassword("mySecurePassword123");

        // Assert
        assertNotNull(hash);
        // BCrypt hashes with a work factor of 12 always start with $2a$12$
        assertTrue(hash.startsWith("$2a$12$"));
        // Standard BCrypt hashes are 60 characters long
        assertTrue(hash.length() == 60);
    }

    @Test
    public void checkPassword_correctPassword_returnsTrue() {
        // Arrange
        String originalPassword = "mySecurePassword123";
        String hash = PasswordManager.hashPassword(originalPassword);

        // Act & Assert
        assertTrue(PasswordManager.checkPassword(originalPassword, hash));
    }

    @Test
    public void checkPassword_incorrectPassword_returnsFalse() {
        // Arrange
        String originalPassword = "mySecurePassword123";
        String wrongPassword = "wrongPassword456";
        String hash = PasswordManager.hashPassword(originalPassword);

        // Act & Assert
        assertFalse(PasswordManager.checkPassword(wrongPassword, hash));
    }

    @Test
    public void checkPassword_invalidOrNullHash_returnsFalse() {
        // Act & Assert
        assertFalse(PasswordManager.checkPassword("password", null));
        assertFalse(PasswordManager.checkPassword("password", "invalid_hash_format"));
        assertFalse(PasswordManager.checkPassword("password", ""));
    }
}