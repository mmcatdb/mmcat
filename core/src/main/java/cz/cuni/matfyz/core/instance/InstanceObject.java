package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.schema.Id;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Each object from instance category is modeled as a set of tuples ({@link ActiveDomainRow}).
 * @author pavel.koupil
 */
public class InstanceObject implements CategoricalObject, JSONConvertible
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

    public List<ActiveDomainRow> rows()
    {
        var output = new ArrayList<ActiveDomainRow>();

        for (var innerMap : activeDomain.values())
            for (ActiveDomainRow row : innerMap.values())
                output.add(row);

        return output;
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
    
    @Override
    public boolean equals(Object object)
    {
        return object instanceof InstanceObject instanceObject && activeDomain.equals(instanceObject.activeDomain);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<InstanceObject> {

        @Override
        protected JSONObject _toJSON(InstanceObject object) throws JSONException {
            var output = new JSONObject();
    
            output.put("key", object.key().toJSON());

            var activeDomain = object.rows().stream().map(row -> row.toJSON()).toList();
            output.put("activeDomain", new JSONArray(activeDomain));
            
            return output;
        }
    
    }
}
