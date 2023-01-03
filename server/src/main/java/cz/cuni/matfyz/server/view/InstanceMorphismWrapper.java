package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SignatureId;

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
