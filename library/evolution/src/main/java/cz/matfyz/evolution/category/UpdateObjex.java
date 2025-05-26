package cz.matfyz.evolution.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;

public record UpdateObjex(
    SerializedObjex newObjex,
    SerializedObjex oldObjex
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema, MetadataCategory metadataCategory) {
        replaceObjex(schema, newObjex.deserialize());
    }

    @Override public void down(SchemaCategory schema, MetadataCategory metadataCategory) {
        replaceObjex(schema, oldObjex.deserialize());
    }

    private void replaceObjex(SchemaCategory schema, SchemaObjex objex) {
        final var objexes = (new SchemaEditor(schema)).getObjexes();
        // Replace the objex by its newer version. The equality is determined by its key.
        objexes.put(objex.key(), objex);

        schema.allMorphisms().forEach(morphism -> morphism.updateObjex(objex));
    }

}
