package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.normalizer.VariableTree;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class collects and provides global information about the whole query.
 */
public class QueryContext {

    // TODO - maybe should be unique per nested clause?

    public QueryContext(SchemaCategory schema, ControlWrapperProvider provider, VariableTree variables) {
        this.schema = schema;
        this.provider = provider;
        this.variables = variables;
    }

    private final VariableTree variables;

    public @Nullable VariableTree getVariables() {
        return variables;
    }

    // Schema category

    private final SchemaCategory schema;

    public SchemaCategory getSchema() {
        return schema;
    }

    // Querying

    private final ControlWrapperProvider provider;

    public ControlWrapperProvider getProvider() {
        return provider;
    }

}
