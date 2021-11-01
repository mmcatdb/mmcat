package cz.cuni.matfyz.core.utils;

/**
 *
 * @author jachymb.bartik
 */
public class ArrayUtils
{
    public static <T> T[] concatenate(T[]... arrays)
    {
        int length = 0;
        for (T[] array : arrays)
            length += array.length;
        
        final T[] output = (T[]) new Object[length];
        
        int startIndex = 0;
        for (T[] array : arrays)
        {
            System.arraycopy(array, 0, output, startIndex, array.length);
            startIndex += array.length;
        }
        
        return output;
    }
    
    /**
     * Why java, why you have to be like this?
     * @param arrays
     * @return 
     */
    public static int[] concatenate(int[]... arrays)
    {
        int length = 0;
        for (int[] array : arrays)
            length += array.length;
        
        final int[] output = new int[length];
        
        int startIndex = 0;
        for (int[] array : arrays)
        {
            System.arraycopy(array, 0, output, startIndex, array.length);
            startIndex += array.length;
        }
        
        return output;
    }
}
