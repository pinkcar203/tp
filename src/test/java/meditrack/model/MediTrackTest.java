package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;

/**
 * Tests for the root MediTrack container, verifying list operations and immutability views.
 */
public class MediTrackTest {

    private MediTrack mediTrack;
    private final Supply testSupply = new Supply("Bandages", 100, LocalDate.of(2027, 1, 1));
    private final Personnel testPersonnel = new Personnel("John Doe", Status.FIT);

    @BeforeEach
    public void setUp() {
        mediTrack = new MediTrack();
    }

    @Test
    public void addPersonnelRecord_addsSuccessfully() {
        mediTrack.addPersonnelRecord(testPersonnel);
        assertEquals(1, mediTrack.getPersonnelList().size());
        assertEquals(testPersonnel, mediTrack.getPersonnelList().get(0));
    }

    @Test
    public void addSupplyRecord_addsSuccessfully() {
        mediTrack.addSupplyRecord(testSupply);
        assertEquals(1, mediTrack.getSupplyList().size());
        assertTrue(mediTrack.hasSupply(testSupply));
    }

    @Test
    public void hasSupply_supplyNotInList_returnsFalse() {
        assertFalse(mediTrack.hasSupply(testSupply));
    }

    @Test
    public void removeSupply_validIndex_removesSuccessfully() {
        mediTrack.addSupply(testSupply);
        Supply removed = mediTrack.removeSupply(0);

        assertEquals(testSupply, removed);
        assertTrue(mediTrack.getSupplyList().isEmpty());
    }

    @Test
    public void setSupply_validIndex_updatesSuccessfully() {
        mediTrack.addSupply(testSupply);
        Supply newSupply = new Supply("Morphine", 10, LocalDate.of(2028, 1, 1));

        mediTrack.setSupply(0, newSupply);

        assertEquals(newSupply, mediTrack.getSupplyList().get(0));
    }

    @Test
    public void addDutySlotRecord_addsSuccessfully() {
        DutySlot slot = new DutySlot(LocalDate.now(), LocalTime.NOON, LocalTime.MIDNIGHT, DutyType.GUARD_DUTY, "John");
        mediTrack.addDutySlotRecord(slot);

        assertEquals(1, mediTrack.getDutySlots().size());
        assertEquals(slot, mediTrack.getDutySlots().get(0));
    }

    @Test
    public void getPersonnelList_modifyList_throwsUnsupportedOperationException() {
        mediTrack.addPersonnelRecord(testPersonnel);
        ObservableList<Personnel> unmodifiableList = mediTrack.getPersonnelList();

        // Assert that the UI cannot accidentally bypass methods to clear the list
        assertThrows(UnsupportedOperationException.class, unmodifiableList::clear);
    }

    @Test
    public void getSupplyList_modifyList_throwsUnsupportedOperationException() {
        mediTrack.addSupply(testSupply);
        ObservableList<Supply> unmodifiableList = mediTrack.getSupplyList();

        // Assert that the UI cannot accidentally bypass methods to remove items
        assertThrows(UnsupportedOperationException.class, () -> unmodifiableList.remove(0));
    }
}