package cz.matfyz.server.entity.schema;

import cz.matfyz.core.category.Morphism.Min;
import cz.matfyz.core.category.Morphism.Tag;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.DisconnectedSchemaMorphism;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

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
    @Nullable Set<Tag> tags
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

    public DisconnectedSchemaMorphism toDisconnectedSchemaMorphism() {
        return new DisconnectedSchemaMorphism(
            signature,
            label,
            domKey,
            codKey,
            min,
            iri,
            pimIri,
            tags
        );
    }

}
