package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;

public record Composite(
    String name
) implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema) {
        /* This function is intentionally empty. */
    }

    @Override public void down(SchemaCategory schema) {
        /* This function is intentionally empty. */
    }

}
