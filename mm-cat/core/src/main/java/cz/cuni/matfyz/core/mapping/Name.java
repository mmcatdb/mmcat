package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class Name
{
    private final String name;
    private final Signature signature;
    private final Type type;
    
    public Name(Signature signature)
    {
        name = "";
        this.signature = signature;
        type = Type.DYNAMIC_NAME;
    }
    
    public Name(String name)
    {
        this.name = name;
        signature = null;
        type = name == "" ? Type.ANONYMOUS : Type.STATIC_NAME;
    }
    
    public static Name Anonymous()
    {
        return new Name("");
    }
    
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
        ANONYMOUS, // Also known as Empty
		DYNAMIC_NAME;
        
        /*
        public cz.cuni.matfyz.core.record.Name.Type toRecordType()
        {
            return switch (this)
            {
                case STATIC_NAME -> cz.cuni.matfyz.core.record.Name.Type.STATIC_NAME;
                case ANONYMOUS -> cz.cuni.matfyz.core.record.Name.Type.ANONYMOUS;
                case DYNAMIC_NAME -> cz.cuni.matfyz.core.record.Name.Type.DYNAMIC_NAME;
            };
        }
        */
	}
    
    public cz.cuni.matfyz.core.record.Name toRecordName(String dynamicNameValue)
    {
        return switch (type)
        {
            case STATIC_NAME -> new cz.cuni.matfyz.core.record.Name(name);
            case ANONYMOUS -> new cz.cuni.matfyz.core.record.Name("");
            case DYNAMIC_NAME -> new cz.cuni.matfyz.core.record.Name(signature, dynamicNameValue);
        };
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
    
    @Override
	public String toString()
    {
        return switch (type)
        {
            case STATIC_NAME -> name;
            case ANONYMOUS -> "_ANONYMOUS";
            case DYNAMIC_NAME -> signature.toString();
        };
    }
}
