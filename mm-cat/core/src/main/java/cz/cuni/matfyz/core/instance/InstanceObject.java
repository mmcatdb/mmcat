package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;

import java.util.*;

/**
 * Each object from instance category is modeled as a set of tuples ({@link ActiveDomainRow}).
 * @author pavel.koupil
 */
public class InstanceObject implements CategoricalObject
{
	private final SchemaObject schemaObject;
	private final Map<SuperIdWithValues, ActiveDomainRow> activeDomain = new HashMap<>();
    
    public Map<SuperIdWithValues, ActiveDomainRow> activeDomain()
    {
		return activeDomain;
	}

	public void addRecord(ActiveDomainRow record)
    {
		activeDomain.put(record.superIdWithValues(), record);
	}

	InstanceObject(SchemaObject schemaObject)
    {
		this.schemaObject = schemaObject;
	}
    
    public Key key()
    {
        return schemaObject.key();
    }
    
	@Override
	public int objectId() {
		return key().getValue();
	}

	@Override
	public int compareTo(CategoricalObject categoricalObject)
    {
        return objectId() - categoricalObject.objectId();
	}
}
