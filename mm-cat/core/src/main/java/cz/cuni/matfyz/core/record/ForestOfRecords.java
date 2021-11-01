package cz.cuni.matfyz.core.record;

import java.util.*;
import java.util.function.Consumer;

/**
 *
 * @author pavel.koupil
 */
public class ForestOfRecords implements Iterable {

	// tady mas list recordu
	private final List<DataRecord> records = new ArrayList<>();
	// a navic tu mas mapu, ktera jako klic ma kategoricky identifikator objektu a jako hodnotu ma ukazatel do recordu, tedy do stromu, na konkretni misto!
	private final Map<Name, Record> quickAccess = new TreeMap<>(); // Name = StaticName | AnonymousName | DerivedName
    
	@Override
	public Iterator iterator() {
		return records.iterator();
	}

	@Override
	public void forEach(Consumer action) {
        records.forEach(action);
	}

	@Override
	public Spliterator spliterator() {
		return records.spliterator();
	}
    
    public void addRecord(DataRecord record)
    {
        records.add(record);
        record.records().forEach(r -> quickAccess.put(r.getName(), r));
    }
}
