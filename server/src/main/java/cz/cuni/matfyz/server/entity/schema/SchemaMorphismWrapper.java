package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Morphism.Tag;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.server.builder.SchemaCategoryContext;

import java.util.Set;

import org.springframework.lang.Nullable;

/**
 * @author jachym.bartik
 */
public record SchemaMorphismWrapper(
    Signature signature,
    String label,
    Key domKey,
    Key codKey,
    Min min,
    String iri,
    String pimIri,
    @Nullable
    Set<Tag> tags
) {

    public static SchemaMorphismWrapper fromSchemaMorphism(SchemaMorphism morphism) {
        return new SchemaMorphismWrapper(
            morphism.signature(),
            morphism.label,
            morphism.dom().key(),
            morphism.cod().key(),
            morphism.min(),
            morphism.iri,
            morphism.pimIri,
            morphism.tags()
        );
    }

    public SchemaMorphism toSchemaMorphism(SchemaCategoryContext context) {
        return new SchemaMorphism.Builder()
            .label(this.label)
            .iri(this.iri)
            .pimIri(this.pimIri)
            .tags(this.tags != null ? this.tags : Set.of())
            .fromArguments(
                signature,
                context.getObject(domKey),
                context.getObject(codKey),
                min
            );
    }
    
}
