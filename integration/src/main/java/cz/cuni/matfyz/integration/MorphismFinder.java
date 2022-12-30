package cz.cuni.matfyz.integration;

import cz.cuni.matfyz.core.category.Morphism.Tag;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jachym.bartik
 */
public class MorphismFinder {

    public static InstanceMorphism findDirectFromObject(InstanceCategory category, String pimIri, InstanceObject object) {
        final var objectMorphismsWithPimIri = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.dom().equals(object))
            .filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri))
            .toList();

        if (objectMorphismsWithPimIri.size() > 1)
            throw new UnsupportedOperationException("Multiple direct morphisms found from object: " + object.key() + " with pim iri: " + pimIri + ".");
        
        return objectMorphismsWithPimIri.size() == 1 ? objectMorphismsWithPimIri.get(0) : null;
    }

    public static InstanceMorphism findFromObject(InstanceCategory category, String pimIri, InstanceObject object) {
        final var algorithm = new MorphismFinder(category, pimIri, object);

        return algorithm.process();
    }

    private final InstanceCategory category;
    private final String pimIri;
    private final InstanceObject object;

    private final Deque<InstanceMorphism> queue = new LinkedList<>();
    private final List<InstanceMorphism> outputCandidates = new ArrayList<>();

    private MorphismFinder(InstanceCategory category, String pimIri, InstanceObject object) {
        this.category = category;
        this.pimIri = pimIri;
        this.object = object;
    }

    private InstanceMorphism process() {
        addObjectMorphismsToQueue(object, Signature.createEmpty());

        while (!queue.isEmpty())
            processPath(queue.poll());

        if (outputCandidates.size() > 1)
            throw new UnsupportedOperationException("Multiple morphisms found from object: " + object.key() + " with pim iri: " + pimIri + ".");
        
        return outputCandidates.size() == 1 ? outputCandidates.get(0) : null;
    }

    private void processPath(InstanceMorphism path) {
        final var lastSection = path.lastBase();

        if (lastSection.schemaMorphism.pimIri.equals(pimIri)) {
            outputCandidates.add(path);
        }
        else if (lastSection.schemaMorphism.hasTag(Tag.isa)) {
            addObjectMorphismsToQueue(path.cod(), path.signature());
        }
    }

    private void addObjectMorphismsToQueue(InstanceObject instanceObject, Signature pathToObject) {
        final var pathBack = pathToObject.getLast().dual();
        final var objectMorphisms = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.dom().equals(instanceObject))
            .filter(morphism -> !morphism.signature().equals(pathBack))
            .toList();

        objectMorphisms.forEach(morphism -> {
            final var newSignature = pathToObject.concatenate(morphism.signature());
            final var newPathMorphism = category.getMorphism(newSignature);

            queue.add(newPathMorphism);
        });
    }

}
