package cz.cuni.matfyz.integration.utils;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;

/**
 * @author jachym.bartik
 */
public class MorphismFinder {

    private final InstanceCategory category;

    public MorphismFinder(InstanceCategory category) {
        this.category = category;
    }

    public InstanceMorphism findDirectFromObject(InstanceObject object, String pimIri) {
        final var matchingMorphisms = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.dom().equals(object))
            .filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri))
            .toList();

        if (matchingMorphisms.size() > 1)
            throw new UnsupportedOperationException("Multiple direct morphisms found from object: " + object.key() + " with pim iri: " + pimIri + ".");
        
        return matchingMorphisms.size() == 1 ? matchingMorphisms.get(0) : null;
    }

    public InstanceMorphism findDirectToObject(InstanceObject object, String pimIri) {
        final var matchingMorphisms = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.cod().equals(object))
            .filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri))
            .toList();

        if (matchingMorphisms.size() > 1)
            throw new UnsupportedOperationException("Multiple direct morphisms found to object: " + object.key() + " with pim iri: " + pimIri + ".");
        
        return matchingMorphisms.size() == 1 ? matchingMorphisms.get(0) : null;
    }

    public InstanceMorphism findFromObject(InstanceObject object, String pimIri) {
        final var algorithm = new FromObjectIsaSearch(category, object, pimIri);

        return algorithm.process();
    }

}
