package cz.matfyz.evolution.schema;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.SchemaCategory;

public class DeleteMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    public final Signature signature;

    public DeleteMorphism(Signature signature) {
        this.signature = signature;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var morphisms = getMorphismContext(category);
        morphisms.deleteUniqueObject(signature);
    }

}
