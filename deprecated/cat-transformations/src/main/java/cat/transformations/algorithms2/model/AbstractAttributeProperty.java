package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.contos
 */
public interface AbstractAttributeProperty extends AbstractProperty {

	// JE ZBYTECNE MIT ABSTRACT VALUE, PREDELEJ TO N
	public abstract AbstractValue getValue();

	public abstract String getName();	// tohle je lepsi resit na urovni AbstractProperty, takze duplicitni a zbytecne je getValue

}
