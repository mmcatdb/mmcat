package cz.cuni.matfyz.core.utils;

/**
 *
 * @author jachymb.bartik
 */
public class Debug
{
    private static int levelValue = 10;
    
    public static int level()
    {
        return levelValue;
    }
    
    public static void setLevel(int level)
    {
        levelValue = level;
    }
    
    public static boolean shouldLog(int level)
    {
        return level >= levelValue;
    }
}
