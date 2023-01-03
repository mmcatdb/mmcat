package cz.cuni.matfyz.integration.utils;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;

/**
 * @author jachym.bartik
 */
public class IsaMorphismCreator {

    private IsaMorphismCreator() {}

    public static DomainRow getOrCreateRowForIsaMorphism(SuperIdWithValues superId, DomainRow initialRow, InstanceMorphism pathToTarget) {
        final var lastIsaRow = getOrCreateLastIsaRow(initialRow, pathToTarget);
        return InstanceObject.getOrCreateRowWithBaseMorphism(superId, lastIsaRow, pathToTarget.lastBase());
    }

    private static DomainRow getOrCreateLastIsaRow(DomainRow initialRow, InstanceMorphism pathToTarget) {
        final var bases = pathToTarget.bases();
        if (bases.isEmpty())
            return initialRow;

        var currentRow = initialRow;
        for (int i = 0; i < bases.size() - 1; i++) {
            final var base = bases.get(i);
            currentRow = InstanceObject.getOrCreateRowWithBaseMorphism(SuperIdWithValues.createEmpty(), currentRow, base);
        }

        return currentRow;
    }

}
