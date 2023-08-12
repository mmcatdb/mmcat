package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.core.mapping.Mapping;

public class KindDefinition {

    public final Mapping mapping;
    public final String databaseId;
    public final AbstractControlWrapper wrapper;

    public KindDefinition(Mapping mapping, String databaseId, AbstractControlWrapper wrapper) {
        this.mapping = mapping;
        this.databaseId = databaseId;
        this.wrapper = wrapper;
    }

}
