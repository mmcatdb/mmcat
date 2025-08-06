package cz.matfyz.core.utils.printable;

import java.util.ArrayDeque;
import java.util.Deque;

class LineStringBuilder implements Printer {

    private int indentationLevel;
    private final String indentationStringPerLevel;
    private final Deque<String> stack = new ArrayDeque<>();

    LineStringBuilder(int indentationLevel, String indentationStringPerLevel) {
        this.indentationLevel = indentationLevel;
        this.indentationStringPerLevel = indentationStringPerLevel;
    }

    LineStringBuilder(int indentationLevel) {
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

    public LineStringBuilder append(Printable printable) {
        final int originalLevel = indentationLevel;
        printable.printTo(this);
        indentationLevel = originalLevel;

        return this;
    }

    public LineStringBuilder append(String string) {
        stack.push(string);
        return this;
    }

    public LineStringBuilder append(int integer) {
        stack.push(Integer.toString(integer));
        return this;
    }

    public LineStringBuilder append(Object object) {
        stack.push(object.toString());
        return this;
    }

    public LineStringBuilder remove() {
        stack.pop();
        return this;
    }

    public LineStringBuilder remove(int index) {
        while (index > 0) {
            stack.pop();
            index--;
        }
        return this;
    }

    @Override public String toString() {
        final var sb = new StringBuilder();

        stack.descendingIterator().forEachRemaining(sb::append);
        final String fullString = sb.toString();

        // So that in the output, there are no whitespaces at the beginning of an empty line. This would make it much harder for string comparison.
        final StringBuilder trimmedSb = new StringBuilder();
        final var split = fullString.split("\n", -1);
        for (int i = 0; i < split.length - 1; i++)
            trimmedSb.append(trimRight(split[i])).append("\n");

        if (split.length > 0)
            trimmedSb.append(trimRight(split[split.length - 1]));

        return trimmedSb.toString();
    }

    private static String trimRight(String input) {
        if (input.length() == 0)
            return input;

        int i = input.length() - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i)))
            i--;

        return input.substring(0, i + 1);
    }

}
