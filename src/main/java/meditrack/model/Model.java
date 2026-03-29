package meditrack.model;

import java.time.LocalDate;
import java.util.List;

import javafx.collections.ObservableList;

import meditrack.commons.core.Index;
import meditrack.logic.commands.exceptions.CommandException;

/** Model facade. */
public interface Model {

    Session getSession();

    /** After login who the user is acting as. */
    void setRole(Role role);

    /** Fails if same supply name already exists. */
    void addSupply(Supply supply);

    void editSupply(Index targetIndex, Supply editedSupply);

    /**
     * Deletes and returns the supply at index.
     */
    Supply deleteSupply(Index targetIndex);

    /**
     * Returns an observable list of all supplies.
     */
    ObservableList<Supply> getFilteredSupplyList();

    /**
     * Returns supplies expiring within daysThreshold days, sorted by expiry
     * date ascending.
     */
    List<Supply> getExpiringSupplies(int daysThreshold);

    /**
     * Returns supplies with quantity below threshold
     */
    List<Supply> getLowStockSupplies(int quantityThreshold);

    /**
     * Returns a read-only view of the underlying MediTrack data
     */
    ReadOnlyMediTrack getMediTrack();

    /**
     * Adds a new personnel member to the roster.
     */
    void addPersonnel(String name, Status status) throws CommandException;

    /**
     * Adds a new personnel member with optional medical profile fields.
     */
    void addPersonnel(String name, Status status, BloodGroup bloodGroup, String allergies)
            throws CommandException;

    /**
     * Removes a personnel member by 1-based index.
     */
    Personnel deletePersonnel(int oneBasedIndex) throws CommandException;

    /**
     * Updates the status of a personnel member by 1-based index.
     */
    void setPersonnelStatus(int oneBasedIndex, Status newStatus) throws CommandException;

    /**
     * Returns a snapshot list of personnel filtered by status (null for all).
     */
    List<Personnel> getFilteredPersonnelList(Status statusFilter);

    /**
     * Returns the live observable personnel list for UI binding.
     */
    ObservableList<Personnel> getPersonnelList();

    /**
     * Returns the total number of personnel in the roster.
     */
    int getPersonnelCount();

    // Duty slot management

    /**
     * Returns an unmodifiable snapshot of scheduled duty slots.
     */
    List<DutySlot> getDutySlots();

    /**
     * Appends a duty slot to the schedule and persists the change.
     */
    void addDutySlot(DutySlot slot);

    /**
     * Removes the duty slot at the given index.
     *
     * @throws CommandException if the index is out of bounds
     */
    void removeDutySlot(int zeroBasedIndex) throws CommandException;

    /**
     * Removes all scheduled duty slots.
     */
    void clearDutySlots();

    /**
     * Removes all duty slots scheduled on the given date.
     */
    void clearDutySlotsForDate(LocalDate date);

    /**
     * Replaces the duty slot at index with newSlot.
     *
     * @throws CommandException if the index is out of bounds
     */
    void replaceDutySlot(int zeroBasedIndex, DutySlot newSlot) throws CommandException;
}