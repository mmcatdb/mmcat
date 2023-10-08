package cz.matfyz.integration.utils;

import cz.matfyz.core.category.Morphism.Tag;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.integration.exception.MorphismException;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author jachym.bartik
 */
public class FromObjectIsaSearch {

    private final InstanceCategory category;
    private final InstanceObject object;
    private final Function<InstanceMorphism, Boolean> findFunction;
    
    public FromObjectIsaSearch(InstanceCategory category, InstanceObject object, Function<InstanceMorphism, Boolean> findFunction) {
        this.category = category;
        this.object = object;
        this.findFunction = findFunction;
    }

    private final Deque<InstanceMorphism> queue = new LinkedList<>();
    private final List<InstanceMorphism> outputCandidates = new ArrayList<>();

    @Nullable
    public InstanceMorphism tryProcess() {
        addObjectMorphismsToQueue(object, Signature.createEmpty());

        while (!queue.isEmpty())
            processPath(queue.poll());

        if (outputCandidates.size() > 1)
            throw MorphismException.multipleFound(object.key());
        
        return outputCandidates.size() == 1 ? outputCandidates.get(0) : null;
    }

    private void processPath(InstanceMorphism path) {
        final var lastSection = path.lastBase();

        if (Boolean.TRUE.equals(findFunction.apply(lastSection))) {
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
