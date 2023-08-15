package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;

public class CreateMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    // dom and cod of the morphism are probably null because they have not been created yet during the creation of this operation
    public final SchemaMorphism morphism;
    public final Key domKey;
    public final Key codKey;

    public CreateMorphism(SchemaMorphism morphism, Key domKey, Key codKey) {
        this.morphism = morphism;
        this.domKey = domKey;
        this.codKey = codKey;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var objects = getObjectContext(category);

        final var morphismWithDomCod = new SchemaMorphism.Builder()
            .label(morphism.label)
            .iri(morphism.iri)
            .pimIri(morphism.pimIri)
            .tags(morphism.tags())
            .fromArguments(
                morphism.signature(),
                objects.getUniqueObject(domKey),
                objects.getUniqueObject(codKey),
                morphism.min()
            );

        getMorphismContext(category).createUniqueObject(morphismWithDomCod);
    }

}
