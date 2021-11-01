package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class InstanceObject implements CategoricalObject
{
	private final Key key;
//	private String label;
	private final List<ActiveDomainRow> activeDomain;
//	private Set<Key> ids;

	public void addRecord(ActiveDomainRow record) {
		activeDomain.add(record);
	}

	public InstanceObject(Key key) {
		this.key = key;
		activeDomain = new ArrayList<>();
	}

	public InstanceObject(SchemaObject object) {
		key = object.key();
		activeDomain = new ArrayList<>();

	}
    
    public Key key()
    {
        return key;
    }
    
	@Override
	public int objectId() {
		return key.getValue();
	}

	@Override
	public int compareTo(CategoricalObject categoricalObject)
    {
        return objectId() - categoricalObject.objectId();
	}
}
