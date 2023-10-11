package cz.matfyz.core.utils;

/**
 * @author jachymb.bartik
 */
public class IndentedStringBuilder {
    
    private final int indentationLevel;
    private final String indentationStringPerLevel;
    private final StringBuilder builder = new StringBuilder();

    public IndentedStringBuilder(int indentationLevel, String indentationStringPerLevel) {
        this.indentationLevel = indentationLevel;
        this.indentationStringPerLevel = indentationStringPerLevel;
    }
    
    public IndentedStringBuilder(int indentationLevel) {
        this(indentationLevel, "    ");
    }
    
    public IndentedStringBuilder createNested() {
        return new IndentedStringBuilder(indentationLevel + 1, indentationStringPerLevel);
    }

    private static String computeTabIndentationString(int level, String string) {
        String is = "";
        for (int i = 0; i < level; i++)
            is += string;
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

        final String indentationString = computeTabIndentationString(indentationLevel, indentationStringPerLevel);
        final var innerBuilder = new StringBuilder();

        final var lines = builderString.lines().toList();
        innerBuilder.append(indentationString).append(lines.get(0));

        for (int i = 1; i < lines.size(); i++)
            innerBuilder.append("\n").append(indentationString).append(lines.get(i));
        
        return innerBuilder.toString();
    }
}
