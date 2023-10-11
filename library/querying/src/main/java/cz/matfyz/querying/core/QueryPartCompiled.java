package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.core.mapping.Mapping;

@Deprecated
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