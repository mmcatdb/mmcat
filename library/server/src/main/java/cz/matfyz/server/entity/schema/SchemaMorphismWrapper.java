package cz.matfyz.server.entity.schema;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.DisconnectedSchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

public record SchemaMorphismWrapper(
    Signature signature,
    String label,
    Key domKey,
    Key codKey,
    Min min,
    @Nullable Set<Tag> tags
) {

    public static SchemaMorphismWrapper fromSchemaMorphism(SchemaMorphism morphism) {
        return new SchemaMorphismWrapper(
            morphism.signature(),
            morphism.label,
            morphism.dom().key(),
            morphism.cod().key(),
            morphism.min(),
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
            tags
        );
    }

}
