package cz.matfyz.core.utils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author jachymb.bartik
 */
public class LineStringBuilder {
    
    private int indentationLevel;
    private final String indentationStringPerLevel;
    private final Deque<String> stack = new ArrayDeque<>();

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
        return string.repeat(level);
    }

    public LineStringBuilder nextLine() {
        stack.push("\n" + computeTabIndentationString(indentationLevel, indentationStringPerLevel));
        return this;
    }
    
    public LineStringBuilder append(String str) {
        stack.push(str);
        return this;
    }
    
    public LineStringBuilder append(int i) {
        stack.push("" + i);
        return this;
    }
    
    public LineStringBuilder append(Object obj) {
        stack.push(obj.toString());
        return this;
    }

    public LineStringBuilder remove() {
        stack.pop();
        return this;
    }

    public LineStringBuilder remove(int i) {
        while (i > 0) {
            stack.pop();
            i--;
        }
        return this;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        
        stack.descendingIterator().forEachRemaining(builder::append);
        final String fullString = builder.toString();

        // So that in the output, there are no whitespaces at the beginning of an empty line. This would make it much harder for string comparison.
        final StringBuilder trimmedBuilder = new StringBuilder();
        final var split = fullString.split("\n", -1);
        for (int i = 0; i < split.length - 1; i++)
            trimmedBuilder.append(trimRight(split[i])).append("\n");
        
        if (split.length > 0)
            trimmedBuilder.append(trimRight(split[split.length - 1]));
        
        return trimmedBuilder.toString();
    }

    private String trimRight(String input) {
        if (input.length() == 0)
            return input;

        int i = input.length() - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i)))
            i--;

        return input.substring(0, i + 1);
    }
    
}
