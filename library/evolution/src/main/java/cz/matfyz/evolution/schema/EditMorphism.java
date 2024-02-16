package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.DisconnectedSchemaMorphism;

public class EditMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    // The dom and cod of the morphism are probably null because they have not been created yet during the creation of this operation.
    public final DisconnectedSchemaMorphism newMorphism;
    public final DisconnectedSchemaMorphism oldMorphism;

    public EditMorphism(DisconnectedSchemaMorphism newMorphism, DisconnectedSchemaMorphism oldMorphism) {
        this.newMorphism = newMorphism;
        this.oldMorphism = oldMorphism;
    }

    @Override public void up(SchemaCategory category) {
        replaceMorphism(category, newMorphism);
    }

    @Override public void down(SchemaCategory category) {
        replaceMorphism(category, oldMorphism);
    }

    private void replaceMorphism(SchemaCategory category, DisconnectedSchemaMorphism morphism) {
        final var objects = getObjectContext(category);
        final var morphismWithObjects = morphism.toSchemaMorphism(objects::getUniqueObject);

        final var morphisms = getMorphismContext(category);
        // Replace the morphism by its newer version. The equality is determined by its signature.
        morphisms.deleteUniqueObject(morphismWithObjects);
        morphisms.createUniqueObject(morphismWithObjects);
    }

}
