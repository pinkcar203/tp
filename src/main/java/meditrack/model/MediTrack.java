package meditrack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Root data container that holds both the supply and personnel lists.
 */
public class MediTrack implements ReadOnlyMediTrack {

    private final ObservableList<Supply> supplies = FXCollections.observableArrayList();
    final ObservableList<Personnel> personnel = FXCollections.observableArrayList();
    private final List<DutySlot> dutySlots = new ArrayList<>();

    /**
     * Appends a Personnel record directly to the internal list.
     * Used by StorageManager when loading data from disk.
     */
    public void addPersonnelRecord(Personnel p) {
        personnel.add(p);
    }

    /**
     * Returns the live mutable personnel list.
     */
    ObservableList<Personnel> getPersonnelObservable() {
        return personnel;
    }

    /** Unmodifiable view of supplies. */
    @Override
    public ObservableList<Supply> getSupplyList() {
        return FXCollections.unmodifiableObservableList(supplies);
    }

    /** Unmodifiable view of personnel. */
    @Override
    public ObservableList<Personnel> getPersonnelList() {
        return FXCollections.unmodifiableObservableList(personnel);
    }

    /** Checks for duplicate supply name. */
    public boolean hasSupply(Supply supply) {
        return supplies.stream().anyMatch(s -> s.equals(supply));
    }

    /** Adds a supply to the inventory. Duplicate names are now allowed for different batches. */
    public void addSupply(Supply supply) {
        supplies.add(supply);
    }

    /** Replaces the supply at the given index. */
    public void setSupply(int index, Supply editedSupply) {
        supplies.set(index, editedSupply);
    }

    /** Removes and returns the supply at the given index. */
    public Supply removeSupply(int index) {
        return supplies.remove(index);
    }

    /**
     * Returns the internal mutable list.
     */
    public ObservableList<Supply> getInternalSupplyList() {
        return supplies;
    }

    /**
     * Appends a supply.
     */
    public void addSupplyRecord(Supply s) {
        supplies.add(s);
    }

    // Duty slots

    /** Unmodifiable view of scheduled duty slots. */
    @Override
    public List<DutySlot> getDutySlots() {
        return Collections.unmodifiableList(dutySlots);
    }

    /** Internal mutable list. */
    List<DutySlot> getDutySlotsInternal() {
        return dutySlots;
    }

    /**
     * Appends a duty slot record.
     */
    public void addDutySlotRecord(DutySlot slot) {
        dutySlots.add(slot);
    }
}