package cz.cuni.matfyz.transformations.algorithms;

/**
 *
 * @author jachymb.bartik
 */
public class UniqueIdProvider
{
    // TODO Examine when this is called and when it should be reseted (probably only for tests, because they require specific id values)

    private static final int lastIdDefault = -1;
    private static int lastId = lastIdDefault;
    
    public static String getNext()
    {
        lastId++;
        return "" + lastId;
    }

    public static void reset()
    {
        lastId = lastIdDefault;
    }
}
