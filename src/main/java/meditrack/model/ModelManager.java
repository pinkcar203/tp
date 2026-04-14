package meditrack.model;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;

import meditrack.commons.core.Index;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.exceptions.InvalidIndexException;

/**
 * Represents the in-memory implementation of the application data model.
 * Coordinates operations between the UI and the underlying MediTrack data structures.
 */
public class ModelManager implements Model {

    private static final String MSG_DUPLICATE = "A personnel member named \"%s\" already exists.";
    private static final String MSG_OUT_OF_BOUNDS = "Index %d is out of bounds. The list currently has %d member(s).";
    private static final String MSG_SLOT_OUT_OF_BOUNDS = "Slot index %d is out of bounds. The roster currently has %d slot(s).";

    private final MediTrack mediTrack;
    private final Session session;
    private Clock clock = Clock.systemDefaultZone();

    /**
     * Constructs a ModelManager initialized with existing MediTrack data.
     *
     * @param mediTrack The populated MediTrack instance.
     */
    public ModelManager(MediTrack mediTrack) {
        this.mediTrack = mediTrack;
        // Instantiating the session locally to avoid global Singleton state
        this.session = new Session();
        cleanExpiredStatuses();
    }

    /**
     * Constructs a ModelManager with an empty MediTrack instance.
     */
    public ModelManager() {
        this(new MediTrack());
    }

    /**
     * Retrieves the clock used for time-dependent operations.
     *
     * @return The current java.time.Clock.
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Injects a custom clock, primarily used for simulating time in unit tests or dev mode.
     *
     * @param clock The java.time.Clock to inject.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Retrieves the active user session.
     *
     * @return The Session instance.
     */
    @Override
    public Session getSession() {
        return session;
    }

    /**
     * Sets the operational role for the current session.
     *
     * @param role The Role to assign.
     */
    @Override
    public void setRole(Role role) {
        session.setRole(role);
    }

    /**
     * Adds a new supply to the inventory.
     *
     * @param supply The supply to add.
     */
    @Override
    public void addSupply(Supply supply) {
        mediTrack.addSupply(supply);
    }

    /**
     * Edits a supply at a specific index.
     *
     * @param targetIndex  The target Index wrapper.
     * @param editedSupply The updated supply object.
     * @throws InvalidIndexException If the index is out of bounds.
     */
    @Override
    public void editSupply(Index targetIndex, Supply editedSupply) {
        int zeroIndex = targetIndex.getZeroBased();
        ObservableList<Supply> internalList = mediTrack.getInternalSupplyList();
        if (zeroIndex < 0 || zeroIndex >= internalList.size()) {
            throw new InvalidIndexException();
        }
        mediTrack.setSupply(zeroIndex, editedSupply);
    }

    /**
     * Deletes a supply at a specific index.
     *
     * @param targetIndex The target Index wrapper.
     * @return The deleted Supply object.
     * @throws InvalidIndexException If the index is out of bounds.
     */
    @Override
    public Supply deleteSupply(Index targetIndex) {
        int zeroIndex = targetIndex.getZeroBased();
        ObservableList<Supply> internalList = mediTrack.getInternalSupplyList();
        if (zeroIndex < 0 || zeroIndex >= internalList.size()) {
            throw new InvalidIndexException();
        }
        return mediTrack.removeSupply(zeroIndex);
    }

    /**
     * Retrieves the observable list of supplies.
     *
     * @return The ObservableList of Supply.
     */
    @Override
    public ObservableList<Supply> getFilteredSupplyList() {
        return mediTrack.getSupplyList();
    }

    /**
     * Retrieves supplies expiring within a specified number of days.
     *
     * @param daysThreshold The day threshold.
     * @return A sorted list of expiring supplies.
     */
    @Override
    public List<Supply> getExpiringSupplies(int daysThreshold) {
        LocalDate today = LocalDate.now(clock);
        LocalDate cutoff = today.plusDays(daysThreshold);
        return mediTrack.getInternalSupplyList().stream()
                .filter(s -> !s.getExpiryDate().isBefore(today) && !s.getExpiryDate().isAfter(cutoff))
                .sorted(Comparator.comparing(Supply::getExpiryDate))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves supplies that fall below a specified quantity threshold.
     *
     * @param quantityThreshold The quantity threshold.
     * @return A sorted list of low-stock supplies.
     */
    @Override
    public List<Supply> getLowStockSupplies(int quantityThreshold) {
        return mediTrack.getInternalSupplyList().stream()
                .filter(s -> s.getQuantity() < quantityThreshold)
                .sorted(Comparator.comparingInt(Supply::getQuantity))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the read-only view of the model data.
     *
     * @return The ReadOnlyMediTrack instance.
     */
    @Override
    public ReadOnlyMediTrack getMediTrack() {
        return mediTrack;
    }

    /**
     * Retrieves the observable list of personnel.
     *
     * @return The ObservableList of Personnel.
     */
    @Override
    public ObservableList<Personnel> getPersonnelList() {
        return mediTrack.getPersonnelList();
    }

    /**
     * Adds a personnel member with basic details.
     *
     * @param name   The name.
     * @param status The status.
     * @throws CommandException If a duplicate exists.
     */
    @Override
    public void addPersonnel(String name, Status status) throws CommandException {
        addPersonnel(name, status, null, "");
    }

    /**
     * Adds a personnel member with extended medical details.
     *
     * @param name       The name.
     * @param status     The status.
     * @param bloodGroup The blood group.
     * @param allergies  The allergies.
     * @throws CommandException If a duplicate exists.
     */
    @Override
    public void addPersonnel(String name, Status status, BloodGroup bloodGroup, String allergies)
            throws CommandException {
        Personnel candidate = new Personnel(name, status, bloodGroup, allergies);
        for (Personnel existing : getInternalPersonnelList()) {
            if (existing.equals(candidate)) {
                throw new CommandException(String.format(MSG_DUPLICATE, name));
            }
        }
        getInternalPersonnelList().add(candidate);
    }

    /**
     * Deletes a personnel member by index.
     *
     * @param oneBasedIndex The 1-based index.
     * @return The deleted Personnel.
     * @throws CommandException If out of bounds.
     */
    @Override
    public Personnel deletePersonnel(int oneBasedIndex) throws CommandException {
        List<Personnel> list = getInternalPersonnelList();
        if (oneBasedIndex < 1 || oneBasedIndex > list.size()) {
            throw new CommandException(String.format(MSG_OUT_OF_BOUNDS, oneBasedIndex, list.size()));
        }
        return list.remove(oneBasedIndex - 1);
    }

    /**
     * Sets the status of a personnel member by index.
     *
     * @param oneBasedIndex The 1-based index.
     * @param newStatus     The new status.
     * @throws CommandException If out of bounds.
     */
    @Override
    public void setPersonnelStatus(int oneBasedIndex, Status newStatus) throws CommandException {
        List<Personnel> list = getInternalPersonnelList();
        if (oneBasedIndex < 1 || oneBasedIndex > list.size()) {
            throw new CommandException(String.format(MSG_OUT_OF_BOUNDS, oneBasedIndex, list.size()));
        }
        list.get(oneBasedIndex - 1).setStatus(newStatus);
    }

    /**
     * Replaces the target personnel with the updated editedPersonnel.
     *
     * @param target          The original Personnel object to be replaced.
     * @param editedPersonnel The updated Personnel object containing the new data.
     */
    @Override
    public void setPersonnel(Personnel target, Personnel editedPersonnel) {
        ObservableList<Personnel> internalList = getInternalPersonnelList();
        int index = internalList.indexOf(target);
        if (index != -1) {
            internalList.set(index, editedPersonnel);
        }
    }

    /**
     * Retrieves personnel filtered by status.
     *
     * @param statusFilter The status to filter by, or null for all.
     * @return The filtered list.
     */
    @Override
    public List<Personnel> getFilteredPersonnelList(Status statusFilter) {
        if (statusFilter == null) {
            return Collections.unmodifiableList(new ArrayList<>(getInternalPersonnelList()));
        }
        return getInternalPersonnelList().stream()
                .filter(p -> p.getStatus() == statusFilter)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Retrieves the total count of personnel.
     *
     * @return The integer count.
     */
    @Override
    public int getPersonnelCount() {
        return getInternalPersonnelList().size();
    }

    /**
     * Internal helper to access the mutable personnel list.
     *
     * @return The mutable ObservableList.
     */
    private ObservableList<Personnel> getInternalPersonnelList() {
        return mediTrack.getPersonnelObservable();
    }

    /**
     * Retrieves the current duty slots.
     *
     * @return A list of DutySlot objects.
     */
    @Override
    public List<DutySlot> getDutySlots() {
        return mediTrack.getDutySlots();
    }

    /**
     * Adds a duty slot.
     *
     * @param slot The DutySlot to add.
     */
    @Override
    public void addDutySlot(DutySlot slot) {
        mediTrack.getDutySlotsInternal().add(slot);
    }

    /**
     * Removes a duty slot by internal index.
     *
     * @param zeroBasedIndex The index to remove.
     * @throws CommandException If out of bounds.
     */
    @Override
    public void removeDutySlot(int zeroBasedIndex) throws CommandException {
        List<DutySlot> slots = mediTrack.getDutySlotsInternal();
        if (zeroBasedIndex < 0 || zeroBasedIndex >= slots.size()) {
            throw new CommandException(String.format(MSG_SLOT_OUT_OF_BOUNDS, zeroBasedIndex, slots.size()));
        }
        slots.remove(zeroBasedIndex);
    }

    /**
     * Clears all duty slots.
     */
    @Override
    public void clearDutySlots() {
        mediTrack.getDutySlotsInternal().clear();
    }

    /**
     * Clears duty slots for a specific date.
     *
     * @param date The date to clear.
     */
    @Override
    public void clearDutySlotsForDate(LocalDate date) {
        mediTrack.getDutySlotsInternal().removeIf(slot -> slot.getDate().equals(date));
    }

    /**
     * Replaces a duty slot at a specific index.
     *
     * @param zeroBasedIndex The index to replace.
     * @param newSlot        The new DutySlot.
     * @throws CommandException If out of bounds.
     */
    @Override
    public void replaceDutySlot(int zeroBasedIndex, DutySlot newSlot) throws CommandException {
        List<DutySlot> slots = mediTrack.getDutySlotsInternal();
        if (zeroBasedIndex < 0 || zeroBasedIndex >= slots.size()) {
            throw new CommandException(String.format(MSG_SLOT_OUT_OF_BOUNDS, zeroBasedIndex, slots.size()));
        }
        slots.set(zeroBasedIndex, newSlot);
    }

    /**
     * Scans the roster for expired medical statuses and automatically reverts them to FIT.
     * Utilizes the injected clock for accurate time checking.
     */
    public void cleanExpiredStatuses() {
        LocalDate today = LocalDate.now(clock);
        List<Personnel> roster = getInternalPersonnelList();

        for (Personnel p : roster) {
            if (p.getStatusExpiryDate() != null && !today.isBefore(p.getStatusExpiryDate())) {
                p.setStatus(Status.FIT);
                p.setStatusExpiryDate(null);
            }
        }
    }
}