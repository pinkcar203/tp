package meditrack.model;

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

/** In-memory model. */
public class ModelManager implements Model {

    private static final String MSG_DUPLICATE =
            "A personnel member named \"%s\" already exists.";
    private static final String MSG_OUT_OF_BOUNDS =
            "Index %d is out of bounds. The list currently has %d member(s).";
    private static final String MSG_SLOT_OUT_OF_BOUNDS =
            "Slot index %d is out of bounds. The roster currently has %d slot(s).";
    private final MediTrack mediTrack;
    private final Session session;

    /**
     * Creates a manager backed by the given MediTrack.
     *
     * @param mediTrack data
     */
    public ModelManager(MediTrack mediTrack) {
        this.mediTrack = mediTrack;
        this.session = Session.getInstance();
    }

    /** Creates a manager with an empty MediTrack. */
    public ModelManager() {
        this(new MediTrack());
    }

    /** Returns the current session. */
    @Override
    public Session getSession() {
        return session;
    }

    /** Sets the active role after login. */
    @Override
    public void setRole(Role role) {
        session.setRole(role);
    }

    /** Adds a supply; duplicate names are rejected. */
    @Override
    public void addSupply(Supply supply) {
        mediTrack.addSupply(supply);
    }

    /** Replaces the supply at index. */
    @Override
    public void editSupply(Index targetIndex, Supply editedSupply) {
        int zeroIndex = targetIndex.getZeroBased();
        ObservableList<Supply> internalList = mediTrack.getInternalSupplyList();
        if (zeroIndex < 0 || zeroIndex >= internalList.size()) {
            throw new InvalidIndexException();
        }
        mediTrack.setSupply(zeroIndex, editedSupply);
    }

    /** Deletes and returns the supply at index. */
    @Override
    public Supply deleteSupply(Index targetIndex) {
        int zeroIndex = targetIndex.getZeroBased();
        ObservableList<Supply> internalList = mediTrack.getInternalSupplyList();
        if (zeroIndex < 0 || zeroIndex >= internalList.size()) {
            throw new InvalidIndexException();
        }
        return mediTrack.removeSupply(zeroIndex);
    }

    /** Returns all supplies as an observable list. */
    @Override
    public ObservableList<Supply> getFilteredSupplyList() {
        return mediTrack.getSupplyList();
    }

    /** Supplies expiring within daysThreshold days, sorted by expiry. */
    @Override
    public List<Supply> getExpiringSupplies(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysThreshold);
        return mediTrack.getInternalSupplyList().stream()
                .filter(s -> !s.getExpiryDate().isBefore(today) && !s.getExpiryDate().isAfter(cutoff))
                .sorted(Comparator.comparing(Supply::getExpiryDate))
                .collect(Collectors.toList());
    }

    /** Supplies below quantityThreshold quantity, sorted ascending. */
    @Override
    public List<Supply> getLowStockSupplies(int quantityThreshold) {
        return mediTrack.getInternalSupplyList().stream()
                .filter(s -> s.getQuantity() < quantityThreshold)
                .sorted(Comparator.comparingInt(Supply::getQuantity))
                .collect(Collectors.toList());
    }

    /** Read-only view of data for saving to disk. */
    @Override
    public ReadOnlyMediTrack getMediTrack() {
        return mediTrack;
    }

    /** Observable personnel list. */
    @Override
    public ObservableList<Personnel> getPersonnelList() {
        return mediTrack.getPersonnelList();
    }

    /** Adds a person to the roster. */
    @Override
    public void addPersonnel(String name, Status status) throws CommandException {
        addPersonnel(name, status, null, "");
    }

    /** Adds a person to the roster with optional blood group and allergies. */
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

    /** Removes the person at the given index. */
    @Override
    public Personnel deletePersonnel(int oneBasedIndex) throws CommandException {
        List<Personnel> list = getInternalPersonnelList();
        if (oneBasedIndex < 1 || oneBasedIndex > list.size()) {
            throw new CommandException(
                    String.format(MSG_OUT_OF_BOUNDS, oneBasedIndex, list.size()));
        }
        return list.remove(oneBasedIndex - 1);
    }

    /** Updates status for the person at the index. */
    @Override
    public void setPersonnelStatus(int oneBasedIndex, Status newStatus) throws CommandException {
        List<Personnel> list = getInternalPersonnelList();
        if (oneBasedIndex < 1 || oneBasedIndex > list.size()) {
            throw new CommandException(
                    String.format(MSG_OUT_OF_BOUNDS, oneBasedIndex, list.size()));
        }
        list.get(oneBasedIndex - 1).setStatus(newStatus);
    }

    /** Personnel filtered by status; null means everyone. */
    @Override
    public List<Personnel> getFilteredPersonnelList(Status statusFilter) {
        if (statusFilter == null) {
            return Collections.unmodifiableList(new ArrayList<>(getInternalPersonnelList()));
        }
        return getInternalPersonnelList().stream()
                .filter(p -> p.getStatus() == statusFilter)
                .collect(Collectors.toUnmodifiableList());
    }

    /** Number of people in the roster. */
    @Override
    public int getPersonnelCount() {
        return getInternalPersonnelList().size();
    }

    private ObservableList<Personnel> getInternalPersonnelList() {
        return mediTrack.getPersonnelObservable();
    }

    // Duty slot management

    @Override
    public List<DutySlot> getDutySlots() {
        return mediTrack.getDutySlots();
    }

    @Override
    public void addDutySlot(DutySlot slot) {
        mediTrack.getDutySlotsInternal().add(slot);
    }

    @Override
    public void removeDutySlot(int zeroBasedIndex) throws CommandException {
        List<DutySlot> slots = mediTrack.getDutySlotsInternal();
        if (zeroBasedIndex < 0 || zeroBasedIndex >= slots.size()) {
            throw new CommandException(
                    String.format(MSG_SLOT_OUT_OF_BOUNDS, zeroBasedIndex, slots.size()));
        }
        slots.remove(zeroBasedIndex);
    }

    @Override
    public void clearDutySlots() {
        mediTrack.getDutySlotsInternal().clear();
    }

    @Override
    public void clearDutySlotsForDate(LocalDate date) {
        mediTrack.getDutySlotsInternal().removeIf(slot -> slot.getDate().equals(date));
    }

    @Override
    public void replaceDutySlot(int zeroBasedIndex, DutySlot newSlot) throws CommandException {
        List<DutySlot> slots = mediTrack.getDutySlotsInternal();
        if (zeroBasedIndex < 0 || zeroBasedIndex >= slots.size()) {
            throw new CommandException(
                    String.format(MSG_SLOT_OUT_OF_BOUNDS, zeroBasedIndex, slots.size()));
        }
        slots.set(zeroBasedIndex, newSlot);
    }
}
