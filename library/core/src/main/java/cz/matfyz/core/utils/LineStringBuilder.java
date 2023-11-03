package cz.matfyz.core.utils;

/**
 * @author jachymb.bartik
 */
public class LineStringBuilder {
    
    private int indentationLevel;
    private final String indentationStringPerLevel;
    private final StringBuilder builder = new StringBuilder();

    public LineStringBuilder(int indentationLevel, String indentationStringPerLevel) {
        this.indentationLevel = indentationLevel;
        this.indentationStringPerLevel = indentationStringPerLevel;
    }
    
    public LineStringBuilder(int indentationLevel) {
        this(indentationLevel, "    ");
    }
    
    public LineStringBuilder down() {
        indentationLevel++;
        return this;
    }

    public LineStringBuilder up() {
        indentationLevel--;
        return this;
    }

    private static String computeTabIndentationString(int level, String string) {
        String is = "";
        for (int i = 0; i < level; i++)
            is += string;
        return is;
    }

    public LineStringBuilder nextLine() {
        builder
            .append("\n")
            .append(computeTabIndentationString(indentationLevel, indentationStringPerLevel));
        return this;
    }
    
    public LineStringBuilder append(String str) {
        builder.append(str);
        return this;
    }
    
    public LineStringBuilder append(int i) {
        builder.append(i);
        return this;
    }
    
    public LineStringBuilder append(Object obj) {
        builder.append(obj);
        return this;
    }
    
    @Override
    public String toString() {
        return builder.toString();
    }
}
