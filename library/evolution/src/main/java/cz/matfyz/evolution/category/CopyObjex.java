package cz.matfyz.evolution.category;

import java.util.List;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;

/**
 * Composite operation that copies an objex along with all its morphisms.
 * This creates a new objex (target) that is a copy of an existing objex
 * (source),
 * including all morphisms and their structure.
 */
public record CopyObjex(
        SerializedObjex source,
        SerializedObjex target,
        List<SMO> children) implements SMO {

    @Override
    public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void up(SchemaCategory schema, MetadataCategory metadata) {
        for (final var child : children)
            child.up(schema, metadata);
    }

    @Override
    public void down(SchemaCategory schema, MetadataCategory metadata) {
        for (final var child : children.reversed())
            child.down(schema, metadata);
    }

}
