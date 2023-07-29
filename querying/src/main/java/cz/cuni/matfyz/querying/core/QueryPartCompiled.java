package cz.cuni.matfyz.querying.core;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.core.mapping.Mapping;

public class QueryPartCompiled {

    public final String query;
    public final Mapping mapping;
    public final AbstractControlWrapper controlWrapper;

    public QueryPartCompiled(String query, Mapping mapping, AbstractControlWrapper controlWrapper) {
        this.query = query;
        this.mapping = mapping;
        this.controlWrapper = controlWrapper;
    }

}