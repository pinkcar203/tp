package meditrack.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/*
 * Equivalence Partitions:
 *
 *  fromZeroBased(int) 
 * Parameter: zeroBasedIndex
 *   Valid:   0 (boundary), positive integer
 *   Invalid: negative (-1)
 *
 *  fromOneBased(int) 
 * Parameter: oneBasedIndex
 *   Valid:   1 (boundary), positive integer
 *   Invalid: 0
 *
 * equals(Object) 
 *   Same instance, same value, different value, null, non-Index object
 */


public class IndexTest {

    @Test
    public void createIndex_negativeZeroBased_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Index.fromZeroBased(-1));
    }

    @Test
    public void createIndex_zeroOneBased_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Index.fromOneBased(0));
    }

    @Test
    public void getZeroBased_fromOneBasedInput_calculatesCorrectly() {
        Index index = Index.fromOneBased(5);
        assertEquals(4, index.getZeroBased());
    }

    @Test
    public void getOneBased_fromZeroBasedInput_calculatesCorrectly() {
        Index index = Index.fromZeroBased(4);
        assertEquals(5, index.getOneBased());
    }

    @Test
    public void equals_sameIndices_returnsTrue() {
        Index i1 = Index.fromOneBased(3);
        Index i2 = Index.fromZeroBased(2); // 3 (1-based) == 2 (0-based)

        assertEquals(i1, i2);
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    public void equals_differentIndices_returnsFalse() {
        Index i1 = Index.fromOneBased(3);
        Index i2 = Index.fromOneBased(4);

        assertNotEquals(i1, i2);
    }

    @Test
    public void toString_returnsOneBasedString() {
        Index index = Index.fromZeroBased(0);
        assertEquals("1", index.toString());
    }

    @Test
    void test_equals_sameInstance_returnsTrue() {
        // Arrange
        Index index = Index.fromZeroBased(3);

        // Act & Assert
        assertEquals(index, index);
    }

    @Test
    void test_equals_nullObject_returnsFalse() {
        // Arrange
        Index index = Index.fromZeroBased(3);

        // Act & Assert
        assertNotEquals(null, index);
    }

    @Test
    void test_equals_nonIndexObject_returnsFalse() {
        // Arrange
        Index index = Index.fromZeroBased(3);
        String notIndex = "3";

        // Act & Assert
        assertNotEquals(index, notIndex);
    }

    @Test
    void test_fromZeroBased_zero_validBoundary() {
        // Arrange & Act
        Index index = Index.fromZeroBased(0);

        // Assert
        assertEquals(0, index.getZeroBased());
        assertEquals(1, index.getOneBased());
    }

    @Test
    void test_fromOneBased_one_validBoundary() {
        // Arrange & Act
        Index index = Index.fromOneBased(1);

        // Assert
        assertEquals(0, index.getZeroBased());
        assertEquals(1, index.getOneBased());
    }

    @Test
    void test_toString_largeIndex_returnsCorrectOneBasedString() {
        // Arrange
        Index index = Index.fromZeroBased(99);

        // Act & Assert
        assertEquals("100", index.toString());
    }
}
