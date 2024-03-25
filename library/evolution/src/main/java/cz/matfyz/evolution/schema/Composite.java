package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;

public class Composite extends SchemaCategory.Editor implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public final String name;

    public Composite(String name) {
        this.name = name;
    }

    @Override public void up(SchemaCategory category) {
        /* This function is intentionally empty. */
    }

    @Override public void down(SchemaCategory category) {
        /* This function is intentionally empty. */
    }

}
