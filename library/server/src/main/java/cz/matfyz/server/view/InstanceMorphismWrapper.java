package cz.matfyz.server.view;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.core.instance.MappingRow;

import java.util.Set;

/**
 * @author jachym.bartik
 */
public record InstanceMorphismWrapper(
    Signature signature,
    Key domKey,
    Key codKey,
    SignatureId domSuperId,
    SignatureId codSuperId,
    Set<MappingRow> mappings
) {
    public InstanceMorphismWrapper(InstanceMorphism morphism) {
        this(
            morphism.signature(),
            morphism.dom().key(),
            morphism.cod().key(),
            morphism.dom().superId(),
            morphism.cod().superId(),
            morphism.allMappings()
        );
    }
}
