package cz.cuni.matfyz.core.category;

/**
 *
 * @author pavel.koupil
 */
public interface Morphism {

	public abstract CategoricalObject dom();

	public abstract CategoricalObject cod();

	public abstract Morphism dual();
	
	public abstract Signature signature();

}
