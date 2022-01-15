package cz.cuni.matfyz.core.utils;

/**
 *
 * @author jachymb.bartik
 */
public class IntendedStringBuilder
{
    private final String intendationString;
    private final StringBuilder builder = new StringBuilder();
    
    public IntendedStringBuilder(String intendationString)
    {
        this.intendationString = intendationString;
    }
    
    public IntendedStringBuilder(int intendationDepth)
    {
        this(getTabIntendationString(intendationDepth));
    }
    
    private static String getTabIntendationString(int depth)
    {
        String is = "";
        for (int i = 0; i < depth; i++)
            is += "    ";
        return is;
    }
    
    public IntendedStringBuilder append(String str)
    {
        builder.append(str);
        return this;
    }
    
    public IntendedStringBuilder append(int i)
    {
        builder.append(i);
        return this;
    }
    
    public IntendedStringBuilder append(Object obj)
    {
        builder.append(obj);
        return this;
    }
    
    @Override
    public String toString()
    {
        String builderString = builder.toString();
        
        var innerBuilder = new StringBuilder();
        for (String line : builderString.lines().toList())
            innerBuilder.append(intendationString).append(line).append("\n");
        
        return innerBuilder.toString();
    }
}
