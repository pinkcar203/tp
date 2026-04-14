package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.commons.core.Index;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.exceptions.InvalidIndexException;


public class ModelManagerTest {

    private ModelManager modelManager;

    @BeforeEach
    public void setUp() {
        modelManager = new ModelManager();
    }

    @Test
    public void cleanExpiredStatuses_mcNotExpired_statusRemainsMc() throws CommandException {
        // Arrange: Add a personnel and set them to MC expiring in 2 days
        modelManager.addPersonnel("John Doe", Status.FIT);
        Personnel john = modelManager.getFilteredPersonnelList(null).get(0);

        john.setStatus(Status.MC);
        john.setStatusExpiryDate(LocalDate.now(modelManager.getClock()).plusDays(2));

        // Act: Run the cleaner logic
        modelManager.cleanExpiredStatuses();

        // Assert: John should still be on MC because time hasn't passed
        assertEquals(Status.MC, john.getStatus());
    }

    @Test
    public void cleanExpiredStatuses_mcExpiredViaTimeTravel_statusRevertsToFit() throws CommandException {
        // Arrange: Add a personnel and set them to MC expiring in 2 days
        modelManager.addPersonnel("Jane Smith", Status.FIT);
        Personnel jane = modelManager.getFilteredPersonnelList(null).get(0);

        jane.setStatus(Status.MC);
        jane.setStatusExpiryDate(LocalDate.now(modelManager.getClock()).plusDays(2));

        // Fast-Forward the clock 3 days into the future! (Dependency Injection Test)
        Clock futureClock = Clock.offset(modelManager.getClock(), Duration.ofDays(3));
        modelManager.setClock(futureClock);

        // Act: Run the cleaner logic using the future clock
        modelManager.cleanExpiredStatuses();

        // Assert: Jane should have automatically reverted to FIT, and expiry date cleared
        assertEquals(Status.FIT, jane.getStatus());
        assertNull(jane.getStatusExpiryDate());
    }

    // --- addPersonnel POSITIVE TESTS (pairwise coverage for 4 params) ---

    @Test
    void test_addPersonnel_validNameFitStatusBloodGroupAllergies_addsSuccessfully() throws CommandException {
        // Arrange
        String validName = "John Doe";
        Status validStatus = Status.FIT;
        BloodGroup validBloodGroup = BloodGroup.A_POS;
        String validAllergies = "Peanuts";

        // Act
        modelManager.addPersonnel(validName, validStatus, validBloodGroup, validAllergies);

        // Assert
        assertEquals(1, modelManager.getPersonnelCount());
        Personnel person = modelManager.getFilteredPersonnelList(null).get(0);
        assertEquals(validName, person.getName());
        assertEquals(validStatus, person.getStatus());
        assertEquals(validBloodGroup, person.getBloodGroup());
        assertEquals(validAllergies, person.getAllergies());
    }

    @Test
    void test_addPersonnel_validNameMcStatusNullBloodGroupEmptyAllergies_addsSuccessfully()
            throws CommandException {
        // Arrange
        String validName = "Jane Smith";
        Status validStatus = Status.MC;
        BloodGroup nullBloodGroup = null;
        String emptyAllergies = "";

        // Act
        modelManager.addPersonnel(validName, validStatus, nullBloodGroup, emptyAllergies);

        // Assert
        assertEquals(1, modelManager.getPersonnelCount());
    }

    @Test
    void test_addPersonnel_basicTwoArgOverload_addsSuccessfully() throws CommandException {
        // Arrange
        String validName = "SimpleGuy";
        Status validStatus = Status.PENDING;

        // Act
        modelManager.addPersonnel(validName, validStatus);

        // Assert
        assertEquals(1, modelManager.getPersonnelCount());
    }

    @Test
    void test_addPersonnel_validNameCasualtyStatusONegBloodGroupNullAllergies_addsSuccessfully()
            throws CommandException {
        // Arrange (pairwise: CASUALTY + O_NEG + null allergies)
        modelManager.addPersonnel("Alpha", Status.CASUALTY, BloodGroup.O_NEG, null);

        // Assert
        assertEquals(1, modelManager.getPersonnelCount());
        assertEquals("", modelManager.getFilteredPersonnelList(null).get(0).getAllergies());
    }

    // --- addPersonnel NEGATIVE TESTS ---

    @Test
    void test_addPersonnel_duplicateNameOnly_throwsCommandException() throws CommandException {
        // Arrange
        modelManager.addPersonnel("John", Status.FIT);

        // Act & Assert
        CommandException ex = assertThrows(CommandException.class, () ->
                modelManager.addPersonnel("John", Status.MC));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void test_addPersonnel_duplicateNameCaseInsensitive_throwsCommandException() throws CommandException {
        // Arrange
        modelManager.addPersonnel("john doe", Status.FIT);

        // Act & Assert
        assertThrows(CommandException.class, () ->
                modelManager.addPersonnel("JOHN DOE", Status.MC));
    }

    // --- deletePersonnel POSITIVE TESTS ---

    @Test
    void test_deletePersonnel_validFirstIndex_removesCorrectPerson() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        modelManager.addPersonnel("Bob", Status.MC);
        int validFirstIndex = 1;

        // Act
        Personnel removed = modelManager.deletePersonnel(validFirstIndex);

        // Assert
        assertEquals("Alice", removed.getName());
        assertEquals(1, modelManager.getPersonnelCount());
    }

    @Test
    void test_deletePersonnel_validLastIndex_removesCorrectPerson() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        modelManager.addPersonnel("Bob", Status.MC);
        int validLastIndex = 2;

        // Act
        Personnel removed = modelManager.deletePersonnel(validLastIndex);

        // Assert
        assertEquals("Bob", removed.getName());
    }

    // --- deletePersonnel NEGATIVE TESTS ---

    @Test
    void test_deletePersonnel_zeroIndex_throwsCommandException() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        int zeroIndex = 0;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.deletePersonnel(zeroIndex));
    }

    @Test
    void test_deletePersonnel_indexBeyondListSize_throwsCommandException() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        int outOfBoundsIndex = 5;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.deletePersonnel(outOfBoundsIndex));
    }

    // --- setPersonnelStatus POSITIVE TESTS ---

    @Test
    void test_setPersonnelStatus_validIndexAndMcStatus_updatesSuccessfully() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        int validIndex = 1;
        Status newStatus = Status.MC;

        // Act
        modelManager.setPersonnelStatus(validIndex, newStatus);

        // Assert
        assertEquals(Status.MC, modelManager.getFilteredPersonnelList(null).get(0).getStatus());
    }

    // --- setPersonnelStatus NEGATIVE TESTS ---

    @Test
    void test_setPersonnelStatus_invalidZeroIndex_throwsCommandException() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        int invalidIndex = 0;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.setPersonnelStatus(invalidIndex, Status.MC));
    }

    @Test
    void test_setPersonnelStatus_indexBeyondList_throwsCommandException() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        int outOfBoundsIndex = 10;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.setPersonnelStatus(outOfBoundsIndex, Status.MC));
    }

    // --- editSupply POSITIVE TESTS ---

    @Test
    void test_editSupply_validIndex_updatesSuccessfully() {
        // Arrange
        modelManager.addSupply(new Supply("OldItem", 10, LocalDate.now().plusDays(30)));
        Index validIndex = Index.fromOneBased(1);
        Supply editedSupply = new Supply("NewItem", 50, LocalDate.now().plusDays(90));

        // Act
        modelManager.editSupply(validIndex, editedSupply);

        // Assert
        assertEquals("NewItem", modelManager.getFilteredSupplyList().get(0).getName());
    }

    // --- editSupply NEGATIVE TESTS ---

    @Test
    void test_editSupply_indexBeyondListSize_throwsInvalidIndexException() {
        // Arrange
        Index outOfBoundsIndex = Index.fromOneBased(5);
        Supply editedSupply = new Supply("Test", 10, LocalDate.now().plusDays(30));

        // Act & Assert
        assertThrows(InvalidIndexException.class, () -> modelManager.editSupply(outOfBoundsIndex, editedSupply));
    }

    // --- deleteSupply NEGATIVE TESTS ---

    @Test
    void test_deleteSupply_indexBeyondListSize_throwsInvalidIndexException() {
        // Arrange
        Index outOfBoundsIndex = Index.fromOneBased(1);

        // Act & Assert
        assertThrows(InvalidIndexException.class, () -> modelManager.deleteSupply(outOfBoundsIndex));
    }

    // --- getExpiringSupplies POSITIVE TESTS ---

    @Test
    void test_getExpiringSupplies_suppliesWithinThreshold_returnsFilteredSorted() {
        // Arrange
        int validDaysThreshold = 30;
        modelManager.addSupply(new Supply("Soon", 100, LocalDate.now().plusDays(10)));
        modelManager.addSupply(new Supply("Later", 100, LocalDate.now().plusDays(20)));
        modelManager.addSupply(new Supply("Far", 100, LocalDate.now().plusDays(365)));

        // Act
        List<Supply> expiring = modelManager.getExpiringSupplies(validDaysThreshold);

        // Assert
        assertEquals(2, expiring.size());
        assertEquals("Soon", expiring.get(0).getName());
        assertEquals("Later", expiring.get(1).getName());
    }

    @Test
    void test_getExpiringSupplies_noSuppliesWithinThreshold_returnsEmptyList() {
        // Arrange
        modelManager.addSupply(new Supply("Far", 100, LocalDate.now().plusDays(365)));
        int shortThreshold = 1;

        // Act
        List<Supply> expiring = modelManager.getExpiringSupplies(shortThreshold);

        // Assert
        assertTrue(expiring.isEmpty());
    }

    @Test
    void test_getExpiringSupplies_expiredSupplyBeforeToday_excluded() {
        // Arrange
        Clock futureClock = Clock.offset(Clock.systemDefaultZone(), Duration.ofDays(100));
        modelManager.setClock(futureClock);
        modelManager.addSupply(new Supply("Expired", 100, LocalDate.now().plusDays(5)));

        // Act
        List<Supply> expiring = modelManager.getExpiringSupplies(30);

        // Assert
        assertTrue(expiring.isEmpty());
    }

    // --- getLowStockSupplies POSITIVE TESTS ---

    @Test
    void test_getLowStockSupplies_belowThreshold_returnsFilteredSorted() {
        // Arrange
        int validThreshold = 50;
        modelManager.addSupply(new Supply("VeryLow", 5, LocalDate.now().plusDays(365)));
        modelManager.addSupply(new Supply("SomewhatLow", 30, LocalDate.now().plusDays(365)));
        modelManager.addSupply(new Supply("Adequate", 100, LocalDate.now().plusDays(365)));

        // Act
        List<Supply> lowStock = modelManager.getLowStockSupplies(validThreshold);

        // Assert
        assertEquals(2, lowStock.size());
        assertEquals("VeryLow", lowStock.get(0).getName());
        assertEquals("SomewhatLow", lowStock.get(1).getName());
    }

    @Test
    void test_getLowStockSupplies_zeroThreshold_returnsEmptyList() {
        // Arrange
        modelManager.addSupply(new Supply("Item", 0, LocalDate.now().plusDays(365)));
        int zeroThreshold = 0;

        // Act
        List<Supply> lowStock = modelManager.getLowStockSupplies(zeroThreshold);

        // Assert
        assertTrue(lowStock.isEmpty());
    }

    // --- getFilteredPersonnelList POSITIVE TESTS ---

    @Test
    void test_getFilteredPersonnelList_nullFilter_returnsAll() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        modelManager.addPersonnel("Bob", Status.MC);

        // Act
        List<Personnel> allPersonnel = modelManager.getFilteredPersonnelList(null);

        // Assert
        assertEquals(2, allPersonnel.size());
    }

    @Test
    void test_getFilteredPersonnelList_fitFilter_returnsOnlyFit() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);
        modelManager.addPersonnel("Bob", Status.MC);
        modelManager.addPersonnel("Charlie", Status.FIT);

        // Act
        List<Personnel> fitOnly = modelManager.getFilteredPersonnelList(Status.FIT);

        // Assert
        assertEquals(2, fitOnly.size());
    }

    @Test
    void test_getFilteredPersonnelList_noMatchingFilter_returnsEmptyList() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);

        // Act
        List<Personnel> casualtyList = modelManager.getFilteredPersonnelList(Status.CASUALTY);

        // Assert
        assertTrue(casualtyList.isEmpty());
    }

    // --- Duty Slot operations ---

    @Test
    void test_addDutySlot_validSlot_addedSuccessfully() {
        // Arrange
        DutySlot validSlot = new DutySlot(LocalDate.now(), LocalTime.of(8, 0),
                LocalTime.of(10, 0), DutyType.GUARD_DUTY, "Alice");

        // Act
        modelManager.addDutySlot(validSlot);

        // Assert
        assertEquals(1, modelManager.getDutySlots().size());
    }

    @Test
    void test_removeDutySlot_validIndex_removesSuccessfully() throws CommandException {
        // Arrange
        DutySlot slot = new DutySlot(LocalDate.now(), LocalTime.of(8, 0),
                LocalTime.of(10, 0), DutyType.GUARD_DUTY, "Alice");
        modelManager.addDutySlot(slot);
        int validZeroIndex = 0;

        // Act
        modelManager.removeDutySlot(validZeroIndex);

        // Assert
        assertTrue(modelManager.getDutySlots().isEmpty());
    }

    @Test
    void test_removeDutySlot_negativeIndex_throwsCommandException() {
        // Arrange
        int negativeIndex = -1;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.removeDutySlot(negativeIndex));
    }

    @Test
    void test_removeDutySlot_indexBeyondSize_throwsCommandException() {
        // Arrange
        int outOfBoundsIndex = 0;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.removeDutySlot(outOfBoundsIndex));
    }

    @Test
    void test_replaceDutySlot_validIndex_replacesSuccessfully() throws CommandException {
        // Arrange
        DutySlot oldSlot = new DutySlot(LocalDate.now(), LocalTime.of(8, 0),
                LocalTime.of(10, 0), DutyType.GUARD_DUTY, "Alice");
        modelManager.addDutySlot(oldSlot);
        DutySlot newSlot = new DutySlot(LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(12, 0), DutyType.PATROL, "Bob");
        int validZeroIndex = 0;

        // Act
        modelManager.replaceDutySlot(validZeroIndex, newSlot);

        // Assert
        assertEquals("Bob", modelManager.getDutySlots().get(0).getPersonnelName());
    }

    @Test
    void test_replaceDutySlot_negativeIndex_throwsCommandException() {
        // Arrange
        DutySlot newSlot = new DutySlot(LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(12, 0), DutyType.PATROL, "Bob");
        int negativeIndex = -1;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.replaceDutySlot(negativeIndex, newSlot));
    }

    @Test
    void test_replaceDutySlot_indexBeyondSize_throwsCommandException() {
        // Arrange
        DutySlot newSlot = new DutySlot(LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(12, 0), DutyType.PATROL, "Bob");
        int outOfBoundsIndex = 5;

        // Act & Assert
        assertThrows(CommandException.class, () -> modelManager.replaceDutySlot(outOfBoundsIndex, newSlot));
    }

    @Test
    void test_clearDutySlots_withExistingSlots_clearsAll() {
        // Arrange
        modelManager.addDutySlot(new DutySlot(LocalDate.now(), LocalTime.of(8, 0),
                LocalTime.of(10, 0), DutyType.GUARD_DUTY, "Alice"));
        modelManager.addDutySlot(new DutySlot(LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(12, 0), DutyType.PATROL, "Bob"));

        // Act
        modelManager.clearDutySlots();

        // Assert
        assertTrue(modelManager.getDutySlots().isEmpty());
    }

    @Test
    void test_clearDutySlotsForDate_matchingDate_removesOnlyMatching() {
        // Arrange
        LocalDate targetDate = LocalDate.of(2026, 5, 1);
        LocalDate otherDate = LocalDate.of(2026, 5, 2);
        modelManager.addDutySlot(new DutySlot(targetDate, LocalTime.of(8, 0),
                LocalTime.of(10, 0), DutyType.GUARD_DUTY, "Alice"));
        modelManager.addDutySlot(new DutySlot(otherDate, LocalTime.of(10, 0),
                LocalTime.of(12, 0), DutyType.PATROL, "Bob"));

        // Act
        modelManager.clearDutySlotsForDate(targetDate);

        // Assert
        assertEquals(1, modelManager.getDutySlots().size());
        assertEquals(otherDate, modelManager.getDutySlots().get(0).getDate());
    }

    @Test
    void test_clearDutySlotsForDate_noMatchingDate_leavesAllIntact() {
        // Arrange
        LocalDate existingDate = LocalDate.of(2026, 5, 1);
        LocalDate nonExistingDate = LocalDate.of(2026, 12, 31);
        modelManager.addDutySlot(new DutySlot(existingDate, LocalTime.of(8, 0),
                LocalTime.of(10, 0), DutyType.GUARD_DUTY, "Alice"));

        // Act
        modelManager.clearDutySlotsForDate(nonExistingDate);

        // Assert
        assertEquals(1, modelManager.getDutySlots().size());
    }

    // --- Clock and session ---

    @Test
    void test_getClock_defaultClock_returnsSystemClock() {
        // Act
        Clock clock = modelManager.getClock();

        // Assert
        assertNotNull(clock);
    }

    @Test
    void test_setRole_validRole_updatesSession() {
        // Arrange
        Role validRole = Role.MEDICAL_OFFICER;

        // Act
        modelManager.setRole(validRole);

        // Assert
        assertEquals(Role.MEDICAL_OFFICER, modelManager.getSession().getRole());
    }

    @Test
    void test_getMediTrack_returnsReadOnlyView() {
        // Act
        ReadOnlyMediTrack readOnly = modelManager.getMediTrack();

        // Assert
        assertNotNull(readOnly);
    }

    @Test
    void test_getPersonnelList_returnsObservableList() throws CommandException {
        // Arrange
        modelManager.addPersonnel("Alice", Status.FIT);

        // Act & Assert
        assertEquals(1, modelManager.getPersonnelList().size());
    }

    @Test
    void test_getPersonnelCount_emptyList_returnsZero() {
        // Act & Assert
        assertEquals(0, modelManager.getPersonnelCount());
    }

    // --- cleanExpiredStatuses with multiple personnel ---

    @Test
    void test_cleanExpiredStatuses_multiplePersonnelMixedExpiry_revertsOnlyExpired()
            throws CommandException {
        // Arrange
        modelManager.addPersonnel("Expired", Status.FIT);
        modelManager.addPersonnel("NotExpired", Status.FIT);
        Personnel expired = modelManager.getFilteredPersonnelList(null).get(0);
        Personnel notExpired = modelManager.getFilteredPersonnelList(null).get(1);

        expired.setStatus(Status.MC);
        expired.setStatusExpiryDate(LocalDate.now().minusDays(1));

        notExpired.setStatus(Status.LIGHT_DUTY);
        notExpired.setStatusExpiryDate(LocalDate.now().plusDays(5));

        // Act
        modelManager.cleanExpiredStatuses();

        // Assert
        assertEquals(Status.FIT, expired.getStatus());
        assertEquals(Status.LIGHT_DUTY, notExpired.getStatus());
    }
}
