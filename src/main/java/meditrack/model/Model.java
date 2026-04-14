package meditrack.model;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.ObservableList;

import meditrack.commons.core.Index;
import meditrack.logic.commands.exceptions.CommandException;

/**
 * Defines the API for the application's in-memory data model.
 * Acts as a centralized facade to manage personnel, supplies, duty slots, and the active session.
 */
public interface Model {

    /**
     * Retrieves the current system clock or the custom injected clock.
     * Used for accurate, testable time-based calculations (like expiration dates).
     *
     * @return The active java.time.Clock instance.
     */
    Clock getClock();

    /**
     * Injects a custom clock into the model.
     * Primarily used for simulating time travel in developer mode or during automated unit testing.
     *
     * @param clock The custom java.time.Clock to inject.
     */
    void setClock(Clock clock);

    /**
     * Scans the personnel roster for expired medical statuses (e.g., MC or Light Duty)
     * and automatically reverts those individuals back to FIT status based on the active clock.
     */
    void cleanExpiredStatuses();

    /**
     * Retrieves the current user session, which contains Role-Based Access Control (RBAC) data.
     *
     * @return The active Session object.
     */
    Session getSession();

    /**
     * Sets the operational role for the current user session after a successful authentication.
     *
     * @param role The Role to assign to the active user.
     */
    void setRole(Role role);

    /**
     * Adds a new medical or logistical supply to the inventory.
     *
     * @param supply The Supply item to append to the list.
     */
    void addSupply(Supply supply);

    /**
     * Replaces the supply at the specified index with a newly edited supply object.
     *
     * @param targetIndex  The wrapper containing the target zero-based index.
     * @param editedSupply The updated Supply object containing the new data.
     */
    void editSupply(Index targetIndex, Supply editedSupply);

    /**
     * Deletes the supply at the specified index and removes it from the inventory.
     *
     * @param targetIndex The wrapper containing the target zero-based index.
     * @return The Supply object that was successfully removed.
     */
    Supply deleteSupply(Index targetIndex);

    /**
     * Retrieves the live, observable list of all supplies.
     * Useful for automatically updating JavaFX TableViews when the underlying data changes.
     *
     * @return An ObservableList containing all Supply items.
     */
    ObservableList<Supply> getFilteredSupplyList();

    /**
     * Retrieves a list of supplies that are set to expire within a specified number of days.
     *
     * @param daysThreshold The cutoff threshold in days from the current date.
     * @return A sorted List of expiring Supply items.
     */
    List<Supply> getExpiringSupplies(int daysThreshold);

    /**
     * Retrieves a list of supplies where the available quantity falls below a specific threshold.
     *
     * @param quantityThreshold The quantity limit designating low stock.
     * @return A sorted List of low-stock Supply items.
     */
    List<Supply> getLowStockSupplies(int quantityThreshold);

    /**
     * Retrieves a read-only view of the underlying MediTrack data structures.
     * 
     * @return The ReadOnlyMediTrack instance.
     */
    ReadOnlyMediTrack getMediTrack();

    /**
     * Adds a new personnel member to the roster with basic required information.
     *
     * @param name   The full name of the personnel.
     * @param status The current medical readiness status.
     * @throws CommandException If a personnel member with the exact same name already exists.
     */
    void addPersonnel(String name, Status status) throws CommandException;

    /**
     * Adds a new personnel member to the roster with extended medical profile details.
     *
     * @param name       The full name of the personnel.
     * @param status     The current medical readiness status.
     * @param bloodGroup The ABO+Rh blood classification.
     * @param allergies  A string detailing any known allergies.
     * @throws CommandException If a personnel member with the exact same name already exists.
     */
    void addPersonnel(String name, Status status, BloodGroup bloodGroup, String allergies) throws CommandException;

    /**
     * Removes a personnel member from the roster based on their UI-facing list position.
     *
     * @param oneBasedIndex The 1-based index visible to the user in the application table.
     * @return The Personnel object that was successfully removed.
     * @throws CommandException If the provided index exceeds the bounds of the current roster.
     */
    Personnel deletePersonnel(int oneBasedIndex) throws CommandException;

    /**
     * Updates the medical readiness status of a specific personnel member.
     *
     * @param oneBasedIndex The 1-based index visible to the user in the application table.
     * @param newStatus     The new Status enum to apply to the individual.
     * @throws CommandException If the provided index exceeds the bounds of the current roster.
     */
    void setPersonnelStatus(int oneBasedIndex, Status newStatus) throws CommandException;

    /**
     * Replaces the target personnel with the updated editedPersonnel.
     * This operation will automatically trigger a refresh on any observing UI components.
     *
     * @param target          The original Personnel object currently in the roster.
     * @param editedPersonnel The updated Personnel object containing the new data.
     */
    void setPersonnel(Personnel target, Personnel editedPersonnel);

    /**
     * Retrieves a static snapshot list of personnel, optionally filtered by a specific status.
     *
     * @param statusFilter The status to filter by, or null to return the entire roster.
     * @return A List containing the matching Personnel objects.
     */
    List<Personnel> getFilteredPersonnelList(Status statusFilter);

    /**
     * Retrieves the live, observable list of all personnel.
     * 
     * @return An ObservableList of all Personnel.
     */
    ObservableList<Personnel> getPersonnelList();

    /**
     * Retrieves the total count of active personnel currently registered in the system.
     *
     * @return The integer count of personnel.
     */
    int getPersonnelCount();

    /**
     * Retrieves an unmodifiable snapshot of all currently scheduled duty slots.
     *
     * @return A List of DutySlot objects.
     */
    List<DutySlot> getDutySlots();

    /**
     * Appends a new, validated duty slot to the unit's schedule.
     *
     * @param slot The DutySlot object to add to the roster.
     */
    void addDutySlot(DutySlot slot);

    /**
     * Removes a duty slot from the schedule based on its internal storage index.
     *
     * @param zeroBasedIndex The internal 0-based array index of the slot to remove.
     * @throws CommandException If the index falls outside the bounds of the schedule array.
     */
    void removeDutySlot(int zeroBasedIndex) throws CommandException;

    /**
     * Instantly clears all scheduled duty slots from the entire roster.
     */
    void clearDutySlots();

    /**
     * Removes all duty slots that fall on a specific calendar date.
     *
     * @param date The LocalDate targeted for clearing.
     */
    void clearDutySlotsForDate(LocalDate date);

    /**
     * Replaces an existing duty slot with a newly modified duty slot.
     *
     * @param zeroBasedIndex The internal 0-based array index of the slot to overwrite.
     * @param newSlot        The updated DutySlot object.
     * @throws CommandException If the index falls outside the bounds of the schedule array.
     */
    void replaceDutySlot(int zeroBasedIndex, DutySlot newSlot) throws CommandException;
}