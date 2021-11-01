package cz.cuni.matfyz.core.schema;

/**
 * This class represents a 'key' of an object as is described in the paper. It's basically just a number with extra steps.
 * @author pavel.koupil, jachym.bartik
 */
public class Key implements Comparable<Key>
{
    private final int value;
    
    public int getValue()
    {
        return value;
    }

	public Key(int value)
    {
		this.value = value;
	}

	@Override
	public int compareTo(Key key)
    {
		return value - key.value;
	}
    
    @Override
	public String toString()
    {
		return value + "";
	}
    
    public boolean equals(Key key)
    {
        return compareTo(key) == 0;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Key key ? equals(key) : false;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + this.value;
        return hash;
    }
}
