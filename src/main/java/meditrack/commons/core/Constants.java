package meditrack.commons.core;

/**
 * Holds shared numbers used in MediTrack so we are not copying literals everywhere.
 */
public class Constants {

    /**
     *  The number of days before we warn that a supply is expiring soon.
     */
    public static final int EXPIRY_THRESHOLD_DAYS = 30;

    /**
     *  The stock count under which we consider a supply to be low stock.
     */
    public static final int LOW_STOCK_THRESHOLD_QUANTITY = 50;

    /**
     *  The stock count at or below which a supply is considered critical (beyond just low).
     */
    public static final int CRITICAL_STOCK_THRESHOLD_QUANTITY = 10;

    /**
     *  Unused; only here so nobody tries to new Constants().
     */
    private Constants() {}
}
