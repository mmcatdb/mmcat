package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.CategoricalObject;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.koupil
 */
public class SchemaObject implements CategoricalObject
{
	private final Key key;
	private final String label;
	private final SuperId superid;
	private final Set<SuperId> ids;
	private double x;
	private double y;

	public SchemaObject(Key key) {
		this(key, "", new SuperId(), new TreeSet<>());
	}

	public SchemaObject(Key key, String label) {
		this(key, label, new SuperId(), new TreeSet<>());
	}

	public SchemaObject(Key key, String label, SuperId superid, Set<SuperId> ids) {
		this.key = key;
		this.label = label;
		this.superid = superid;
		this.ids = ids;
	}

	public SchemaObject(Key key, String label, SuperId superid, Set<SuperId> ids, double x, double y) {
		this(key, label, superid, ids);
		this.x = x;
		this.y = y;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

    public Key key()
    {
        return key;
    }
    
	@Override
	public int objectId()
    {
		return key.getValue();
	}

	public String label() {
		return label;
	}

	public SuperId superid() {
		return superid;
	}

	public Set<SuperId> ids() {
		return ids;
	}

	@Override
	public int compareTo(CategoricalObject categoricalObject)
    {
        return objectId() - categoricalObject.objectId();
	}
    
    public boolean equals(CategoricalObject categoricalObject)
    {
        return compareTo(categoricalObject) == 0;
    }

	@Override
	public boolean equals(Object obj)
    {
        return obj instanceof CategoricalObject categoricalObject ? equals(categoricalObject) : false;
	}

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        return hash;
    }

	@Override
	public String toString()
    {
        throw new UnsupportedOperationException();
	}
}
