package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.DisconnectedSchemaMorphism;

public class CreateMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    // The dom and cod objects of the morphism are probably null because they have not been created yet during the creation of this operation.
    public final DisconnectedSchemaMorphism morphism;

    public CreateMorphism(DisconnectedSchemaMorphism morphism) {
        this.morphism = morphism;
    }

    @Override public void up(SchemaCategory category) {
        final var objects = getObjects(category);
        final var morphismWithObjects = morphism.toSchemaMorphism(objects::get);

        getMorphisms(category).put(morphismWithObjects.signature(), morphismWithObjects);
    }

    @Override public void down(SchemaCategory category) {
        getMorphisms(category).remove(morphism.signature());
    }

}
