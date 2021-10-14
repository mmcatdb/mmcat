package cz.cuni.matfyz.core.record;

/**
 * Simple property cannot have children so it is a leaf node in the record tree.
 * However, it can have value.
 * @param <DataType> a type of the value of this property.
 */
public class SimpleProperty<DataType> extends Property
{
	private final DataType value;
    
    SimpleProperty(String name, DataType value, ComplexProperty parent)
    {
        super(name, parent);
        this.value = value;
    }
	
    public DataType getValue()
    {
        return value;
    }
}
