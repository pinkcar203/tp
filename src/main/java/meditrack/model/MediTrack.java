package meditrack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The root data container for the MediTrack application.
 * Holds and manages the internal lists of supplies, personnel, and scheduled duty slots.
 */
public class MediTrack implements ReadOnlyMediTrack {

    private final ObservableList<Supply> supplies = FXCollections.observableArrayList();
    private final ObservableList<Personnel> personnel = FXCollections.observableArrayList();
    private final List<DutySlot> dutySlots = new ArrayList<>();

    /**
     * Appends a Personnel record directly to the internal list.
     * Primarily used by the Storage layer when loading saved data from disk.
     *
     * @param p The personnel record to add.
     */
    public void addPersonnelRecord(Personnel p) {
        personnel.add(p);
    }

    /**
     * Retrieves the live, mutable observable list of personnel.
     * Package-private access restricts direct manipulation to the model package.
     *
     * @return The mutable ObservableList of Personnel.
     */
    ObservableList<Personnel> getPersonnelObservable() {
        return personnel;
    }

    /**
     * Retrieves an unmodifiable view of the supply list.
     * Ensures external components (like the UI) cannot alter the data directly.
     *
     * @return An unmodifiable ObservableList of Supply.
     */
    @Override
    public ObservableList<Supply> getSupplyList() {
        return FXCollections.unmodifiableObservableList(supplies);
    }

    /**
     * Retrieves an unmodifiable view of the personnel list.
     * Ensures external components cannot alter the roster directly.
     *
     * @return An unmodifiable ObservableList of Personnel.
     */
    @Override
    public ObservableList<Personnel> getPersonnelList() {
        return FXCollections.unmodifiableObservableList(personnel);
    }

    /**
     * Checks if a specific supply already exists in the inventory.
     *
     * @param supply The supply to check for.
     * @return {@code true} if an identical supply exists, {@code false} otherwise.
     */
    public boolean hasSupply(Supply supply) {
        return supplies.stream().anyMatch(s -> s.equals(supply));
    }

    /**
     * Adds a supply to the inventory.
     * Duplicate names are permitted to allow for batch tracking (different expiry dates).
     *
     * @param supply The supply item to add.
     */
    public void addSupply(Supply supply) {
        supplies.add(supply);
    }

    /**
     * Replaces the supply at the specified index with an updated supply object.
     *
     * @param index        The 0-based index of the supply to edit.
     * @param editedSupply The new supply data.
     */
    public void setSupply(int index, Supply editedSupply) {
        supplies.set(index, editedSupply);
    }

    /**
     * Removes and returns the supply at the specified index.
     *
     * @param index The 0-based index of the supply to remove.
     * @return The removed Supply object.
     */
    public Supply removeSupply(int index) {
        return supplies.remove(index);
    }

    /**
     * Retrieves the internal mutable observable list of supplies.
     *
     * @return The mutable ObservableList of Supply.
     */
    public ObservableList<Supply> getInternalSupplyList() {
        return supplies;
    }

    /**
     * Appends a supply record directly to the internal list.
     * Primarily used by the Storage layer when loading saved data from disk.
     *
     * @param s The supply record to add.
     */
    public void addSupplyRecord(Supply s) {
        supplies.add(s);
    }

    @Override
    public List<DutySlot> getDutySlots() {
        return Collections.unmodifiableList(dutySlots);
    }

    /**
     * Retrieves the internal mutable list of duty slots.
     * Package-private access restricts direct manipulation to the model package.
     *
     * @return The mutable List of DutySlot.
     */
    List<DutySlot> getDutySlotsInternal() {
        return dutySlots;
    }

    /**
     * Appends a duty slot directly to the internal list.
     * Primarily used by the Storage layer when loading saved data from disk.
     *
     * @param slot The duty slot to add.
     */
    public void addDutySlotRecord(DutySlot slot) {
        dutySlots.add(slot);
    }
}
