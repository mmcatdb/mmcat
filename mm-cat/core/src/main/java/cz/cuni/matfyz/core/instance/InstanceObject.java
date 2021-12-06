package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.schema.Id;

import java.util.*;

/**
 * Each object from instance category is modeled as a set of tuples ({@link ActiveDomainRow}).
 * @author pavel.koupil
 */
public class InstanceObject implements CategoricalObject
{
	private final SchemaObject schemaObject;
	//private final Map<IdWithValues, ActiveDomainRow> activeDomain = new TreeMap<>();
    private final Map<Id, Map<IdWithValues, ActiveDomainRow>> activeDomain = new TreeMap<>();
    
    //public Map<IdWithValues, ActiveDomainRow> activeDomain()
    public Map<Id, Map<IdWithValues, ActiveDomainRow>> activeDomain()
    {
		return activeDomain;
	}

    /*
	public void addRecord(ActiveDomainRow record)
    {
		activeDomain.put(record.idWithValues(), record);
	}
    */

	InstanceObject(SchemaObject schemaObject)
    {
		this.schemaObject = schemaObject;
	}
    
    public Key key()
    {
        return schemaObject.key();
    }
    
    public SchemaObject schemaObject()
    {
        return schemaObject;
    }
    
	@Override
	public int objectId()
    {
		return key().getValue();
	}

	@Override
	public int compareTo(CategoricalObject categoricalObject)
    {
        return objectId() - categoricalObject.objectId();
	}
	
	@Override
	public String toString()
    {
		StringBuilder builder = new StringBuilder();

		builder.append("\tKey: ").append(key()).append("\n");
        builder.append("\tValues:\n");
		for (Id id : activeDomain.keySet())
        {
            var subdomain = activeDomain.get(id);
            for (IdWithValues idWithValues : subdomain.keySet())
                builder.append("\t\t").append(subdomain.get(idWithValues)).append("\n");
        }
        
        return builder.toString();
	}
}
