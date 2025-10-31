package cz.matfyz.core.rsd;

import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class DataType {

    private static int numberOfTypes = 0;

    public static final int UNKNOWN = computeType(0);
    public static final int OBJECT = computeType(1);
    public static final int STRING = computeType(2);
    public static final int BOOLEAN = computeType(3);
    public static final int NUMBER = computeType(4);
    public static final int ARRAY = computeType(5);
    public static final int MAP = computeType(6);
    public static final int DATE = computeType(7);

    private static int computeType(int index) {
        numberOfTypes = Math.max(numberOfTypes, index + 1);
        return index == 0
            ? 0
            : (1 << (index - 1));
    }

    private static int computeIndex(int type) {
        return type == 0
            ? 0
            // This function maps 1 -> 0, 2 -> 1, 4 -> 2, ...
            // It's an intrinsic function so it's going to be translated to a single CPU instruction.
            : Integer.numberOfTrailingZeros(type) + 1;
    }

    public static class DataTypeMap<TItem> {

        private final Object[] items = new Object[numberOfTypes];

        public @Nullable TItem get(int type) {
            return (TItem) items[computeIndex(type)];
        }

        public void set(int type, @Nullable TItem item) {
            items[computeIndex(type)] = item;
        }

        public void forEach(Consumer<? super @Nullable TItem> action) {
            for (int i = 0; i < items.length; i++)
                action.accept((TItem) items[i]);
        }

    }

}
