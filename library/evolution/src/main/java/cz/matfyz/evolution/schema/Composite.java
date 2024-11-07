package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;

import java.util.List;

public record Composite(
    String name,
    List<SMO> children
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema) {
        for (final var child : children)
            child.up(schema);
    }

    @Override public void down(SchemaCategory schema) {
        for (final var child : children.reversed())
            child.down(schema);
    }

}
