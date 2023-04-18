package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaCategory;

public class DeleteMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    final Signature signature;

    public DeleteMorphism(Signature signature) {
        this.signature = signature;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var morphisms = getMorphismContext(category);
        morphisms.deleteUniqueObject(signature);
    }

}
