package cz.cuni.matfyz.core.record;

import java.util.*;
import java.util.function.Consumer;

/**
 *
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
    
    public void addRecord(RootRecord record)
    {
        records.add(record);
        //record.records().forEach(r -> quickAccess.put(r.getName(), r));
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        String comma = "";
        for (RootRecord record : records)
        {
            builder.append(comma);
            comma = ",\n";
            builder.append(record);
        }
        builder.append("\n");
        
        return builder.toString();
    }
}
