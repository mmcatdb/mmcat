package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author pavel.koupil
 */
public class Context
{
    private final Signature signature;
    
    public Signature signature()
    {
        return signature;
    }

/*    
	public Type getType()
    {
        return signature == null ? Type.UNDEFINED : Type.SIGNATURE;
    }

	public enum Type
    {
		SIGNATURE,
        UNDEFINED
	}
*/
    
    public Context()
    {
		this.signature = null;
	}
    
	public Context(Signature signature)
    {
		this.signature = signature;
	}
}
