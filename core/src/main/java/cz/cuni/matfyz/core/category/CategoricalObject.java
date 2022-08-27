package cz.cuni.matfyz.core.category;

import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.Key;

import java.util.Set;

/**
 * @author pavel.koupil
 */
public interface CategoricalObject extends Comparable<CategoricalObject> {

    public Key key();

    public String label();

    public Id superId();

    /**
     * Immutable.
     */
    public Set<Id> ids();
    
}
