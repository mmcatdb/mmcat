package cz.matfyz.core.category;

import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SignatureId;

/**
 * @author pavel.koupil
 */
public interface CategoricalObject extends Comparable<CategoricalObject> {

    Key key();

    String label();

    SignatureId superId();

    /**
     * Immutable.
     */
    ObjectIds ids();

    @Override default int compareTo(CategoricalObject categoricalObject) {
        return key().compareTo(categoricalObject.key());
    }

}
