package cz.cuni.matfyz.transformations;

/**
 *
 * @author jachymb.bartik
 */
public class UniqueIdProvider
{
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
