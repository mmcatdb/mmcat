package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author jachym.bartik
 */
public class StaticName extends Name
{
    private final String value;
    private final Type type;
    
    public StaticName(String name)
    {
        super();
        this.value = name;
        this.type = Type.STATIC_NAME;
    }

    // Anonymous name
    private StaticName()
    {
        super();
        this.value = "";
        this.type = Type.ANONYMOUS;
    }

    private static StaticName anonymous = new StaticName();
    
    public static StaticName Anonymous()
    {
        return anonymous;
    }
    
    /*
    public Type type()
    {
        return type;
    }
    */

	public enum Type
    {
		STATIC_NAME,
        ANONYMOUS, // Also known as Empty
	}
    
    public cz.cuni.matfyz.core.record.Name toRecordName()
    {
        return new cz.cuni.matfyz.core.record.Name(value, type);
    }
    
    public String getStringName()
    {
        return switch (type)
        {
            case STATIC_NAME -> value;
            case ANONYMOUS -> "";
        };
    }
    
    @Override
	public String toString()
    {
        return switch (type)
        {
            case STATIC_NAME -> value;
            case ANONYMOUS -> "_ANONYMOUS";
        };
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof StaticName staticName
            ? type == staticName.type && value.equals(staticName.value)
            : false;
    }
}
