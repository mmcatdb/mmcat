package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.normalizer.VariableTree;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class collects and provides global information about the whole query.
 */
public class QueryContext {

    public final VariableTree variables;

    public QueryContext(VariableTree variables) {
        this.variables = variables;
    }

    // Schema category

    // TODO - should be unique per nested clause. However, the variables probably should be as well, so ...

    private @Nullable SchemaCategory schema;

    public @Nullable SchemaCategory getSchema() {
        return schema;
    }

    public QueryContext setSchema(SchemaCategory schema) {
        this.schema = schema;

        return this;
    }

    // Querying

    private @Nullable ControlWrapperProvider provider;

    public @Nullable ControlWrapperProvider getProvider() {
        return provider;
    }

    public QueryContext setProvider(ControlWrapperProvider provider) {
        this.provider = provider;

        return this;
    }

}
