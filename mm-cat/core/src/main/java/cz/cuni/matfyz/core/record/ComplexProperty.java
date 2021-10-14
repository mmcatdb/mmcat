package cz.cuni.matfyz.core.record;

import java.util.List;
import java.util.ArrayList;

/**
 * This class represents an inner node of the record tree.
 * The value of this property are its children.
 */
public class ComplexProperty extends Property
{
    private final List<Property> children = new ArrayList<>();
    
	protected ComplexProperty(String name, ComplexProperty parent)
    {
		super(name, parent);
	}
    
    public List<Property> getChildren()
    {
        return children;
    }
    
    public ComplexProperty addComplexProperty(String name)
    {
        ComplexProperty property = new ComplexProperty(name, this);
        children.add(property);
        return property;
    }
    
    public <DataType> SimpleProperty<DataType> addSimpleProperty(String name, DataType value)
    {
        SimpleProperty property = new SimpleProperty(name, value, this);
        children.add(property);
        return property;
    }
}
