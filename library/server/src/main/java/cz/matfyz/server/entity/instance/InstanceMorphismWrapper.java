package cz.matfyz.server.entity.instance;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.server.entity.instance.InstanceCategoryWrapper.WrapperContext;

import java.util.ArrayList;
import java.util.List;

public record InstanceMorphismWrapper(
    Signature signature,
    List<MappingRowWrapper> mappings
) {

    public static InstanceMorphismWrapper fromInstanceMorphism(InstanceMorphism morphism, WrapperContext context) {
        final var domRowToId = context.rowToId.get(morphism.dom().key());
        final var codRowToId = context.rowToId.get(morphism.cod().key());
        final List<MappingRowWrapper> mappings = new ArrayList<>();

        for (var mapping : morphism.allMappings()) {
            final int dom = domRowToId.get(mapping.domainRow());
            final int cod = codRowToId.get(mapping.codomainRow());
            mappings.add(new MappingRowWrapper(dom, cod));
        }

        return new InstanceMorphismWrapper(
            morphism.signature(),
            mappings
        );
    }

    public void toInstanceMorphism(WrapperContext context) {
        final var morphism = context.category.getMorphism(signature);

        final var domIdToRow = context.idToRow.get(morphism.dom().key());
        final var codIdToRow = context.idToRow.get(morphism.cod().key());

        for (final MappingRowWrapper mappingWrapper : mappings) {
            final DomainRow domRow = domIdToRow.get(mappingWrapper.dom);
            final DomainRow codRow = codIdToRow.get(mappingWrapper.cod);

            morphism.createMapping(domRow, codRow);
        }
    }

    public record MappingRowWrapper(
        int dom,
        int cod
    ) {}

}
