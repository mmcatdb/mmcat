package cz.cuni.matfyz.transformations;

/**
 *
 * @author jachymb.bartik
 */
public class UniqueIdProvider
{
    private static int lastId = -1;
    
    public static String getNext()
    {
        lastId++;
        return "" + lastId;
    }
}
