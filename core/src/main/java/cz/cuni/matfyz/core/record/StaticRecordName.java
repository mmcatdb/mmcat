package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.mapping.StaticName.Type;

/**
 *
 * @author jachym.bartik
 */
public class StaticRecordName extends RecordName
{
	private final Type type;
    
    public StaticRecordName(String value, Type type)
    {
        super(value);
        this.type = type;
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof StaticRecordName staticName
            && value.equals(staticName.value)
            && type.equals(staticName.type);
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    /*
    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.value);
        hash = 17 * hash + Objects.hashCode(this.type);
        return hash;
    }
    */
    
    @Override
	public String toString()
    {
        return switch (type)
        {
            case STATIC_NAME -> value;
            case ANONYMOUS -> "_";
        };
    }
}
