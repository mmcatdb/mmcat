package cz.matfyz.core.record;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author pavel.koupil
 */
public class ForestOfRecords implements Iterable<RootRecord> {

    // tady mas list recordu
    private final List<RootRecord> records = new ArrayList<>();
    // This map should map a categorical identifier (Name) of each property in given kind to the list of respective nodes in the forest.
    // However, this is not very useful because we need to find one value for one particular record instead.
    // private final Map<Name, DataRecord> quickAccess = new TreeMap<>(); // Name = StaticName | AnonymousName | DerivedName
    
    @Override
    public Iterator<RootRecord> iterator() {
        return records.iterator();
    }

    @Override
    public void forEach(Consumer<? super RootRecord> action) {
        records.forEach(action);
    }

    @Override
    public Spliterator<RootRecord> spliterator() {
        return records.spliterator();
    }
    
    public void addRecord(RootRecord rootRecord) {
        records.add(rootRecord);
        //record.records().forEach(r -> quickAccess.put(r.getName(), r));
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String comma = "";
        for (RootRecord rootRecord : records) {
            builder.append(comma);
            comma = ",\n";
            builder.append(rootRecord);
        }
        builder.append("\n");
        
        return builder.toString();
    }

    public int size() {
        return records.size();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;
            
        if (!(object instanceof ForestOfRecords forestOfRecords))
            return false;

        if (records.size() != forestOfRecords.size())
            return false;

        for (final var record1 : records) {
            final var match = forestOfRecords.records.stream().anyMatch(record2 -> record2.equals(record1));
            if (!match)
                return false;
        }

        return true;
    }
}
