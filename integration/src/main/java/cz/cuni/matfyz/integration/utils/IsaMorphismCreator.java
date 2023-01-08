package cz.cuni.matfyz.integration.utils;

import cz.cuni.matfyz.core.category.Morphism.Tag;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;

/**
 * @author jachym.bartik
 */
public class IsaMorphismCreator {

    private IsaMorphismCreator() {}

    /**
     * The morphism is expected to be a list of base isa morphisms, followed by exactly one non-isa morphism.
     * @param superId
     * @param initialRow
     * @param pathToTarget
     * @return
     */
    public static DomainRow getOrCreateRowForIsaMorphism(SuperIdWithValues superId, DomainRow initialRow, InstanceMorphism pathToTarget) {
        final var lastIsaRow = getOrCreateLastIsaRow(initialRow, pathToTarget);
        return InstanceObject.getOrCreateRowWithBaseMorphism(superId, lastIsaRow, pathToTarget.lastBase());
    }

    public static DomainRow connectRowWithIsaMorphism(DomainRow domainRow, DomainRow initialRow, InstanceMorphism pathToTarget) {
        final var lastIsaRow = getOrCreateLastIsaRow(initialRow, pathToTarget);
        return InstanceObject.connectRowWithBaseMorphism(domainRow, lastIsaRow, pathToTarget.lastBase());
    }

    public static DomainRow getOrCreateLastIsaRow(DomainRow initialRow, InstanceMorphism pathToTarget) {
        var currentRow = initialRow;

        for (final var base : pathToTarget.bases()) {
            if (!base.schemaMorphism.hasTag(Tag.isa))
                break;

            currentRow = InstanceObject.getOrCreateRowWithBaseMorphism(SuperIdWithValues.createEmpty(), currentRow, base);
        }

        return currentRow;
    }

}
