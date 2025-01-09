package cz.matfyz.core.record;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ForestOfRecords implements Iterable<RootRecord> {

    private final List<RootRecord> records = new ArrayList<>();
    // This map should map a categorical identifier (Name) of each property in given kind to the list of respective nodes in the forest.
    // However, this is not very useful because we need to find one value for one particular record instead.

    @Override public Iterator<RootRecord> iterator() {
        return records.iterator();
    }

    @Override public void forEach(Consumer<? super RootRecord> action) {
        records.forEach(action);
    }

    @Override public Spliterator<RootRecord> spliterator() {
        return records.spliterator();
    }

    public void addRecord(RootRecord rootRecord) {
        records.add(rootRecord);
    }

    public int size() {
        return records.size();
    }

    @Override public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final RootRecord rootRecord : records)
            builder.append(rootRecord).append(",\n");

        return builder.toString();
    }

    public String toComparableString() {
        return records.stream().map(RootRecord::toComparableString).collect(Collectors.joining(",\n"));
    }

}
