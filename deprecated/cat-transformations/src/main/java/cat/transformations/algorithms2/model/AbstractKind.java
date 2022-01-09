package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.contos
 */
public interface AbstractKind {

	public abstract String getName();

	public abstract Iterable<String> getPropertyNames();	// ke zvazeni, jestli tohle potrebujeme tady...

	public abstract Iterable<AbstractRecordProperty> getRecords();

	public abstract AbstractRecordProperty getRecord(int index);

	public abstract AbstractRecordProperty getRecord(AbstractIdentifier identifier);

	public abstract int size();
	
	public abstract void add(AbstractRecordProperty record);

}
