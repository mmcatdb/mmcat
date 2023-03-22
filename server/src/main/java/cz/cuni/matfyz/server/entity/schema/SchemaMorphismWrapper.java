package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.core.category.Morphism.Max;
import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Morphism.Tag;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.server.builder.SchemaCategoryContext;

import java.util.Set;

/**
 * @author jachym.bartik
 */
public record SchemaMorphismWrapper(
    Signature signature,
    String label,
    Key domKey,
    Key codKey,
    Min min,
    Max max,
    String iri,
    String pimIri,
    Set<Tag> tags
) {

    public static SchemaMorphismWrapper fromSchemaMorphism(SchemaMorphism morphism) {
        return new SchemaMorphismWrapper(
            morphism.signature(),
            morphism.label,
            morphism.dom().key(),
            morphism.cod().key(),
            morphism.min(),
            morphism.max(),
            morphism.iri,
            morphism.pimIri,
            morphism.tags()
        );
    }

    public SchemaMorphism toSchemaMorphism(SchemaCategoryContext context) {
        return new SchemaMorphism.Builder().fromArguments(
            signature,
            context.getObject(domKey),
            context.getObject(codKey),
            min,
            max,
            label
        );
    }
    
}
