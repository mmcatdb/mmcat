package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.DisconnectedSchemaMorphism;

public class CreateMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    // The dom and cod objects of the morphism are probably null because they have not been created yet during the creation of this operation.
    public final DisconnectedSchemaMorphism morphism;

    public CreateMorphism(DisconnectedSchemaMorphism morphism) {
        this.morphism = morphism;
    }

    @Override
    public void up(SchemaCategory category) {
        final var objects = getObjectContext(category);
        final var morphismWithObjects = morphism.toSchemaMorphism(objects::getUniqueObject);

        getMorphismContext(category).createUniqueObject(morphismWithObjects);
    }

    @Override
    public void down(SchemaCategory category) {
        getMorphismContext(category).deleteUniqueObject(morphism.signature());
    }

}
