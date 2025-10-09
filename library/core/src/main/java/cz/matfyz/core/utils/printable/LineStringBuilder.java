package cz.matfyz.core.utils.printable;

import java.util.ArrayDeque;
import java.util.Deque;

import org.checkerframework.checker.nullness.qual.Nullable;

class LineStringBuilder implements Printer {

    private int indentationLevel;
    private final String indentationStringPerLevel;
    private final @Nullable Stringifier stringifier;

    LineStringBuilder(int indentationLevel, String indentationStringPerLevel, @Nullable Stringifier stringifier) {
        this.indentationLevel = indentationLevel;
        this.indentationStringPerLevel = indentationStringPerLevel;
        this.stringifier = stringifier;
    }

    LineStringBuilder(int indentationLevel, @Nullable Stringifier stringifier) {
        this(indentationLevel, "    ", stringifier);
    }

    @Override public LineStringBuilder down() {
        indentationLevel++;
        return this;
    }

    @Override public LineStringBuilder up() {
        indentationLevel--;
        return this;
    }

    private final Deque<String> stack = new ArrayDeque<>();

    @Override public LineStringBuilder nextLine() {
        stack.push("\n" + indentationStringPerLevel.repeat(indentationLevel));
        return this;
    }

    @Override public LineStringBuilder append(Printable printable) {
        final int originalLevel = indentationLevel;
        printable.printTo(this);
        indentationLevel = originalLevel;

        return this;
    }

    @Override public LineStringBuilder append(String string) {
        stack.push(string);
        return this;
    }

    @Override public LineStringBuilder append(int integer) {
        stack.push(Integer.toString(integer));
        return this;
    }

    @Override public LineStringBuilder append(Object object) {
        final var stringValue = stringifier == null
            ? object.toString()
            : stringifier.apply(object);

        stack.push(stringValue);
        return this;
    }

    @Override public LineStringBuilder remove() {
        stack.pop();
        return this;
    }

    @Override public LineStringBuilder remove(int index) {
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
