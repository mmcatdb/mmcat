package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class Name
{
    private String name;
    private Signature signature;

	private Type type;
    
    public Type type()
    {
        return type;
    }
    
    public Signature signature()
    {
        return signature;
    }

	public enum Type
    {
		STATIC_NAME,
        ANONYMOUS,
		DYNAMIC_NAME;
        
        public cz.cuni.matfyz.core.record.Name.Type toRecordType()
        {
            return switch (this)
            {
                case STATIC_NAME -> cz.cuni.matfyz.core.record.Name.Type.STATIC_NAME;
                case ANONYMOUS -> cz.cuni.matfyz.core.record.Name.Type.ANONYMOUS;
                case DYNAMIC_NAME -> cz.cuni.matfyz.core.record.Name.Type.DYNAMIC_NAME;
            };
        }
	}
    
    public cz.cuni.matfyz.core.record.Name toRecordName()
    {
        return new cz.cuni.matfyz.core.record.Name(name, type.toRecordType());
    }
    
    public String getStringName() throws Exception
    {
        return switch (type)
        {
            case STATIC_NAME -> name;
            case ANONYMOUS -> "";
            case DYNAMIC_NAME -> throw new Exception();
        };
    }
}
