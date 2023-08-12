package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;

public class Composite extends SchemaCategory.Editor implements SchemaModificationOperation {

    final String name;

    public Composite(String name) {
        this.name = name;
    }

    @Override
    public void apply(SchemaCategory category) {
        /* This function is intentionally empty. */
    }

}
