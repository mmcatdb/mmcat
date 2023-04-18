package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;

public class AddMorphism implements SchemaModificationOperation {

    // dom and cod of the morphism are probably null because they have not been created yet during the creation of this operation
    final SchemaMorphism morphism;
    final Key domKey;
    final Key codKey;

    public AddMorphism(SchemaMorphism morphism, Key domKey, Key codKey) {
        this.morphism = morphism;
        this.domKey = domKey;
        this.codKey = codKey;
    }

    @Override
    public void apply(SchemaCategory category) {
        category.addMorphism(new SchemaMorphism.Builder()
            .label(morphism.label)
            .iri(morphism.iri)
            .pimIri(morphism.pimIri)
            .tags(morphism.tags())
            .fromArguments(
                morphism.signature(),
                category.getObject(domKey),
                category.getObject(codKey),
                morphism.min()
            )
        );
    }

}
