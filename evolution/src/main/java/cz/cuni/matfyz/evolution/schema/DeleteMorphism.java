package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;

public class DeleteMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    final SchemaMorphism morphism;
    final Key domKey;
    final Key codKey;

    public DeleteMorphism(SchemaMorphism morphism, Key domKey, Key codKey) {
        this.morphism = morphism;
        this.domKey = domKey;
        this.codKey = codKey;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var morphisms = getMorphismContext(category);
        morphisms.deleteUniqueObject(morphism.signature());
    }

}
