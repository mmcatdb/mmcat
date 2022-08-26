package cz.cuni.matfyz.core.utils;

/**
 * @author jachymb.bartik
 */
public class IndentedStringBuilder {
    
    private final String indentationString;
    private final StringBuilder builder = new StringBuilder();
    
    public IndentedStringBuilder(String indentationString) {
        this.indentationString = indentationString;
    }
    
    public IndentedStringBuilder(int indentationDepth) {
        this(getTabIndentationString(indentationDepth));
    }
    
    private static String getTabIndentationString(int depth) {
        String is = "";
        for (int i = 0; i < depth; i++)
            is += "    ";
        return is;
    }
    
    public IndentedStringBuilder append(String str) {
        builder.append(str);
        return this;
    }
    
    public IndentedStringBuilder append(int i) {
        builder.append(i);
        return this;
    }
    
    public IndentedStringBuilder append(Object obj) {
        builder.append(obj);
        return this;
    }
    
    @Override
    public String toString() {
        String builderString = builder.toString();
        
        var innerBuilder = new StringBuilder();
        for (String line : builderString.lines().toList())
            innerBuilder.append(indentationString).append(line).append("\n");
        
        return innerBuilder.toString();
    }
}
