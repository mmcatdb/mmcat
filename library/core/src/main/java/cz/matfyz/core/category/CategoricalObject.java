package cz.matfyz.core.category;

import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SignatureId;

/**
 * @author pavel.koupil
 */
public interface CategoricalObject extends Comparable<CategoricalObject> {

    public Key key();

    public String label();

    public SignatureId superId();

    /**
     * Immutable.
     */
    public ObjectIds ids();

    @Override public default int compareTo(CategoricalObject categoricalObject) {
        return key().compareTo(categoricalObject.key());
    }
    
}
