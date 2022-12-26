package cz.cuni.matfyz.core.category;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SignatureId;

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
    
}
