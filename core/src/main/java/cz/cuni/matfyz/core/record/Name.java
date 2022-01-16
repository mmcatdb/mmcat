package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

import java.util.Objects;

/**
 *
 * @author jachym.bartik
 */
public class Name implements Comparable<Name>
{
    private final String name;
    private final Signature signature;
	private final Type type;
    
	public enum Type
    {
		STATIC_NAME,
        ANONYMOUS,
		DYNAMIC_NAME
	}

    public String value()
    {
        return name;
    }
    
    public Signature signature()
    {
        return signature;
    }
    
    public static Name LeftDynamic()
    {
        return new Name("_LEFT_DYNAMIC", cz.cuni.matfyz.core.mapping.StaticName.Type.STATIC_NAME);
    }
    
    public Name(Signature signature, String name)
    {
        this.name = name;
        this.signature = signature;
        this.type = Type.DYNAMIC_NAME;
    }
    
    public Name(String name, cz.cuni.matfyz.core.mapping.StaticName.Type type)
    {
        this.name = name;
        this.signature = null;
        this.type = type == cz.cuni.matfyz.core.mapping.StaticName.Type.STATIC_NAME ? Type.STATIC_NAME : Type.ANONYMOUS;
    }
    
    public boolean equals(Name anotherName)
    {
        return name.equals(anotherName.name);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Name anotherName ? equals(anotherName) : false;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    @Override
    public int compareTo(Name anotherName)
    {
        return name.compareTo(anotherName.name);
    }
    
    @Override
	public String toString()
    {
        return switch (type)
        {
            case STATIC_NAME -> name;
            case ANONYMOUS -> "_ANONYMOUS";
            case DYNAMIC_NAME -> "\"" + name + "\"";
        };
    }
}
