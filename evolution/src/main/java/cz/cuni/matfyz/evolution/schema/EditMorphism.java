package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;

public class EditMorphism extends SchemaCategory.Editor implements SchemaModificationOperation {

    // dom and cod of the morphism are probably null because they have not been created yet during the creation of this operation
    final SchemaMorphism newMorphism;
    final Key domKey;
    final Key codKey;

    public EditMorphism(SchemaMorphism newMorphism, Key domKey, Key codKey) {
        this.newMorphism = newMorphism;
        this.domKey = domKey;
        this.codKey = codKey;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var objects = getObjectContext(category);

        final var newMorphismWithDomCod = new SchemaMorphism.Builder()
            .label(newMorphism.label)
            .iri(newMorphism.iri)
            .pimIri(newMorphism.pimIri)
            .tags(newMorphism.tags())
            .fromArguments(
                newMorphism.signature(),
                objects.getUniqueObject(domKey),
                objects.getUniqueObject(codKey),
                newMorphism.min()
            );

        final var morphismContext = getMorphismContext(category);
        // Replace the morphism by its newer version. The equality is determined by its signature.
        morphismContext.deleteUniqueObject(newMorphismWithDomCod);
        morphismContext.createUniqueObject(newMorphismWithDomCod);
    }

}
