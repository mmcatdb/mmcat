package cz.matfyz.core.utils;

/**
 * @author jachymb.bartik
 */
public class UniqueIdProvider {

    // TODO Examine when this is called and when it should be reseted (probably only for tests, because they require specific id values)

    private static final int LAST_ID_DEFAULT = -1;
    private static int lastId = LAST_ID_DEFAULT;

    private UniqueIdProvider() {}
    
    public static String getNext() {
        lastId++;
        return "" + lastId;
    }

    public static void reset() {
        lastId = LAST_ID_DEFAULT;
    }
}
