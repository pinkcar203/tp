package meditrack.model;

import java.util.List;

import javafx.collections.ObservableList;

/**
 * Unmodifiable view of the MediTrack data.
 */
public interface ReadOnlyMediTrack {
    /** Returns an unmodifiable observable list of supplies. */
    ObservableList<Supply> getSupplyList();

    /** Returns an unmodifiable observable list of personnel. */
    ObservableList<Personnel> getPersonnelList();

    /** Returns an unmodifiable view of scheduled duty slots. */
    List<DutySlot> getDutySlots();
}
