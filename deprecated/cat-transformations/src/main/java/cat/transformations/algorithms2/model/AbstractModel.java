package cat.transformations.algorithms2.model;

import cat.transformations.algorithms2.schema.AbstractSchema;

/**
 *
 * @author pavel.contos
 */
public interface AbstractModel {

	public abstract Iterable<String> getKindNames();

	public abstract AbstractKind getKind(String name);

	public abstract Iterable<AbstractKind> getKinds();

	public abstract void putKind(String name, AbstractKind kind);

	public abstract boolean isSchemaRequired();

	public abstract AbstractSchema getSchema();

}
