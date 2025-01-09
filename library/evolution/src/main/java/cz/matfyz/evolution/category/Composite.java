package cz.matfyz.evolution.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.List;

public record Composite(
    String name,
    List<SMO> children
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema, MetadataCategory metadata) {
        for (final var child : children)
            child.up(schema, metadata);
    }

    @Override public void down(SchemaCategory schema, MetadataCategory metadata) {
        for (final var child : children.reversed())
            child.down(schema, metadata);
    }

}
