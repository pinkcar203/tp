package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.commons.core.Index;
import meditrack.model.exceptions.InvalidIndexException;

class ModelManagerTest {

    private ModelManager modelManager;

    @BeforeEach
    void setUp() {
        modelManager = new ModelManager();
    }

    @Test
    void addSupply_validSupply_success() {
        Supply supply = new Supply("Bandages", 100, LocalDate.of(2027, 6, 1));
        modelManager.addSupply(supply);
        assertEquals(1, modelManager.getFilteredSupplyList().size());
        assertEquals("Bandages", modelManager.getFilteredSupplyList().get(0).getName());
    }

    @Test
    void addSupply_differentNames_success() {
        Supply supply1 = new Supply("Bandages", 100, LocalDate.of(2027, 6, 1));
        Supply supply2 = new Supply("Panadol", 50, LocalDate.of(2028, 1, 1));

        modelManager.addSupply(supply1);
        modelManager.addSupply(supply2);
        assertEquals(2, modelManager.getFilteredSupplyList().size());
    }

    @Test
    void editSupply_validIndex_success() {
        modelManager.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));
        Supply edited = new Supply("Bandages XL", 200, LocalDate.of(2028, 1, 1));

        modelManager.editSupply(Index.fromOneBased(1), edited);

        assertEquals("Bandages XL", modelManager.getFilteredSupplyList().get(0).getName());
        assertEquals(200, modelManager.getFilteredSupplyList().get(0).getQuantity());
    }

    @Test
    void editSupply_indexOutOfBounds_throwsInvalidIndexException() {
        modelManager.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        Supply edited = new Supply("Bandages XL", 200, LocalDate.of(2028, 1, 1));
        assertThrows(InvalidIndexException.class, () ->
                modelManager.editSupply(Index.fromOneBased(5), edited));
    }

    @Test
    void editSupply_zeroIndex_throwsInvalidIndexException() {
        modelManager.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        Supply edited = new Supply("Updated", 50, LocalDate.of(2028, 1, 1));
        assertThrows(IllegalArgumentException.class, () ->
                modelManager.editSupply(Index.fromOneBased(0), edited));
    }

    @Test
    void deleteSupply_validIndex_returnsDeletedSupply() {
        modelManager.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        Supply deleted = modelManager.deleteSupply(Index.fromOneBased(1));
        assertEquals("Bandages", deleted.getName());
        assertTrue(modelManager.getFilteredSupplyList().isEmpty());
    }

    @Test
    void deleteSupply_indexOutOfBounds_throwsInvalidIndexException() {
        assertThrows(InvalidIndexException.class, () ->
                modelManager.deleteSupply(Index.fromOneBased(1)));
    }

    @Test
    void getFilteredSupplyList_emptyInitially() {
        assertTrue(modelManager.getFilteredSupplyList().isEmpty());
    }

    @Test
    void getFilteredSupplyList_reflectsAdditions() {
        modelManager.addSupply(new Supply("A", 10, LocalDate.of(2027, 1, 1)));
        modelManager.addSupply(new Supply("B", 20, LocalDate.of(2027, 2, 1)));
        assertEquals(2, modelManager.getFilteredSupplyList().size());
    }

    @Test
    void getExpiringSupplies_withinThreshold_returned() {
        modelManager.addSupply(new Supply("Soon", 10, LocalDate.now().plusDays(5)));
        modelManager.addSupply(new Supply("Later", 10, LocalDate.now().plusDays(365)));

        List<Supply> expiring = modelManager.getExpiringSupplies(30);
        assertEquals(1, expiring.size());
        assertEquals("Soon", expiring.get(0).getName());
    }

    @Test
    void getExpiringSupplies_sortedByExpiryAscending() {
        modelManager.addSupply(new Supply("B", 10, LocalDate.now().plusDays(20)));
        modelManager.addSupply(new Supply("A", 10, LocalDate.now().plusDays(5)));
        modelManager.addSupply(new Supply("C", 10, LocalDate.now().plusDays(10)));

        List<Supply> expiring = modelManager.getExpiringSupplies(30);
        assertEquals(3, expiring.size());
        assertEquals("A", expiring.get(0).getName());
        assertEquals("C", expiring.get(1).getName());
        assertEquals("B", expiring.get(2).getName());
    }

    @Test
    void getExpiringSupplies_noneExpiring_emptyList() {
        modelManager.addSupply(new Supply("Safe", 10, LocalDate.now().plusDays(365)));

        List<Supply> expiring = modelManager.getExpiringSupplies(30);
        assertTrue(expiring.isEmpty());
    }

    @Test
    void getLowStockSupplies_belowThreshold_returned() {
        modelManager.addSupply(new Supply("Low", 5, LocalDate.of(2027, 6, 1)));
        modelManager.addSupply(new Supply("Enough", 100, LocalDate.of(2027, 6, 1)));

        List<Supply> lowStock = modelManager.getLowStockSupplies(20);
        assertEquals(1, lowStock.size());
        assertEquals("Low", lowStock.get(0).getName());
    }

    @Test
    void getLowStockSupplies_sortedByQuantityAscending() {
        modelManager.addSupply(new Supply("B", 15, LocalDate.of(2027, 6, 1)));
        modelManager.addSupply(new Supply("A", 3, LocalDate.of(2027, 6, 1)));
        modelManager.addSupply(new Supply("C", 10, LocalDate.of(2027, 6, 1)));

        List<Supply> lowStock = modelManager.getLowStockSupplies(20);
        assertEquals(3, lowStock.size());
        assertEquals("A", lowStock.get(0).getName());
        assertEquals("C", lowStock.get(1).getName());
        assertEquals("B", lowStock.get(2).getName());
    }

    @Test
    void getLowStockSupplies_noneBelow_emptyList() {
        modelManager.addSupply(new Supply("Plenty", 100, LocalDate.of(2027, 6, 1)));

        List<Supply> lowStock = modelManager.getLowStockSupplies(20);
        assertTrue(lowStock.isEmpty());
    }

    @Test
    void getSession_returnsNonNull() {
        assertNotNull(modelManager.getSession());
    }

    @Test
    void setRole_updatesSession() {
        modelManager.setRole(Role.FIELD_MEDIC);
        assertEquals(Role.FIELD_MEDIC, modelManager.getSession().getRole());
    }
}
