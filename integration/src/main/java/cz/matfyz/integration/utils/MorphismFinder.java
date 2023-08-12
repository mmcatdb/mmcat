package cz.matfyz.integration.utils;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.integration.exception.MorphismException;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author jachym.bartik
 */
public class MorphismFinder {

    private final InstanceCategory category;

    public MorphismFinder(InstanceCategory category) {
        this.category = category;
    }

    private Map<ObjectIriTuple, InstanceMorphism> fromDirectObjectCache = new TreeMap<>();

    public InstanceMorphism findDirectFromObject(InstanceObject object, String pimIri) {
        final var tuple = new ObjectIriTuple(object, pimIri);
        if (fromDirectObjectCache.containsKey(tuple))
            return fromDirectObjectCache.get(tuple);

        final var matchingMorphisms = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.dom().equals(object))
            .filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri))
            .toList();

        if (matchingMorphisms.size() > 1)
            throw MorphismException.multipleDirectFound(pimIri, object.key());
        
        final var result = matchingMorphisms.size() == 1 ? matchingMorphisms.get(0) : null;
        fromDirectObjectCache.put(tuple, result);

        return result;
    }

    private Map<ObjectIriTuple, InstanceMorphism> toDirectObjectCache = new TreeMap<>();

    public InstanceMorphism findDirectToObject(InstanceObject object, String pimIri) {
        final var tuple = new ObjectIriTuple(object, pimIri);
        if (toDirectObjectCache.containsKey(tuple))
            return toDirectObjectCache.get(tuple);

        final var matchingMorphisms = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.cod().equals(object))
            .filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri))
            .toList();

        if (matchingMorphisms.size() > 1)
            throw MorphismException.multipleDirectFound(pimIri, object.key());
        
        final var result = matchingMorphisms.size() == 1 ? matchingMorphisms.get(0) : null;
        toDirectObjectCache.put(tuple, result);

        return result;
    }

    private Map<ObjectIriTuple, InstanceMorphism> fromObjectCache = new TreeMap<>();

    public InstanceMorphism findFromObject(InstanceObject object, String pimIri) {
        final Function<InstanceMorphism, Boolean> findFunction = morphism -> morphism.schemaMorphism.pimIri.equals(pimIri);
        return findFromObjectCached(object, pimIri, findFunction);
    }

    private Map<String, InstanceMorphism> pimIriCache = new TreeMap<>();

    private InstanceMorphism findBaseByPimIriNotCached(String pimIri) {
        final var result = category.morphisms().values().stream().filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri)).findFirst();
        if (!result.isPresent())
            throw MorphismException.notFound(pimIri);

        return result.get();
    }

    public InstanceMorphism findBaseByPimIri(String pimIri) {
        return pimIriCache.computeIfAbsent(pimIri, this::findBaseByPimIriNotCached);
    }

    public InstanceMorphism findFromObjectToObject(InstanceObject fromObject, InstanceObject toObject) {
        final Function<InstanceMorphism, Boolean> findFunction = morphism -> morphism.cod().equals(toObject);
        return findFromObjectCached(fromObject, "toObject:" + toObject.schemaObject.pimIri, findFunction);
    }

    private InstanceMorphism findFromObjectCached(InstanceObject object, String key, Function<InstanceMorphism, Boolean> findFunction) {
        final var tuple = new ObjectIriTuple(object, key);
        if (fromObjectCache.containsKey(tuple))
            return fromObjectCache.get(tuple);

        final var algorithm = new FromObjectIsaSearch(category, object, findFunction);
        final var result = algorithm.process();
        fromObjectCache.put(tuple, result);

        return result;
    }

    private record ObjectIriTuple(
        InstanceObject object,
        String pimIri
    ) implements Comparable<ObjectIriTuple> {

        @Override
        public int compareTo(ObjectIriTuple tuple) {
            int objectResult = object.compareTo(tuple.object);
            if (objectResult != 0)
                return objectResult;

            return pimIri.compareTo(tuple.pimIri);
        }

    }

}
