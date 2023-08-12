package cz.cuni.matfyz.querying.core;

import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.VariableIdentifier;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.utils.Common;
import cz.cuni.matfyz.core.utils.Common.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class which builds the mapping which describes the result of a native database query corresponding to a single query part.
 */
public class MappingBuilder {

    public static record PathWithMapping(
        List<AccessPath> path,
        Mapping mapping
    ) {}

    public Map<VariableIdentifier, PathWithMapping> variableDefinitions = new TreeMap<>();

    /**
     * Make the `MappingBuilder` remember that the `variableId` is associated with a specific property path within a given mapping.
     * This information is necessary for when the final mapping is built.
     */
    public void defineVariable(VariableIdentifier variableId, List<AccessPath> propertyPath, Mapping mapping) {
        variableDefinitions.put(variableId, new PathWithMapping(propertyPath, mapping));
    }

    /**
     * Given a map of variable IDs to names along the corresponding property path, build a mapping representing the native result for a single query part.
     */
    public Mapping buildMapping(Map<VariableIdentifier, List<String>> variableMap) {
        var rootKindMapping = variableDefinitions.values().stream().findFirst().get().mapping;

        return new Mapping(
            rootKindMapping.category(),
            rootKindMapping.rootObject().key(),
            rootKindMapping.kindName(),
            createAccessPath(variableMap),
            rootKindMapping.primaryKey()
        );
    }

    private static record PathSegment(String name, Signature signature) implements Comparable<PathSegment> {
        @Override public int compareTo(PathSegment other) {
            return signature.compareTo(other.signature);
        }
    }

    public ComplexProperty createAccessPath(Map<VariableIdentifier, List<String>> variableMap) {
        final List<List<PathSegment>> paths = variableMap.entrySet().stream().map(entry -> createPathDefinition(
            variableDefinitions.get(entry.getKey()).path,
            entry.getValue()
        )).toList();

        final Tree<PathSegment> variableTree = Common.getTreeFromLists(paths);
        // final var subpaths = variableMap.entrySet().stream().map(entry -> buildPropertySubpath(entry.getKey(), entry.getValue(), variableTree)).toArray(AccessPath[]::new);
        
        return ComplexProperty.createRoot(createSubpaths(variableTree));
    }

    private List<PathSegment> createPathDefinition(List<AccessPath> originalPath, List<String> names) {
        final var output = new ArrayList<PathSegment>();
        for (int i = 0; i < names.size(); i++)
            output.add(new PathSegment(names.get(i), originalPath.get(i).signature()));

        return output;
    }

    private List<AccessPath> createSubpaths(Tree<PathSegment> tree) {
        return tree.value.entrySet().stream().map(entry -> {
            final PathSegment segment = entry.getKey();
            final var name = new StaticName(segment.name);

            return entry.getValue().value.isEmpty()
                ? new SimpleProperty(name, segment.signature)
                : new ComplexProperty(name, segment.signature, segment.signature.isEmpty(), createSubpaths(entry.getValue()));
        }).toList();
    }

    // private List<PathSegment> buildPath(List<AccessPath> originalPath, List<String> names) {
    //     final var output = new ArrayList<PathSegment>();
    //     for (int i = 0; i < names.size(); i++)
    //         output.add(new PathSegment(names.get(i), originalPath.get(i).signature()));

    //     return output;
    // }

    // private AccessPath buildPropertySubpath(VariableIdentifier variableId, List<String> variableNamePath, Tree<String> variableTree) {
    //     var path = variableDefinitions.get(variableId).path;
    //     AccessPath currentProperty = new SimpleProperty(
    //         new StaticName(variableNamePath.get(variableNamePath.size() - 1)),
    //         path.get(path.size() - 1).signature()
    //     );
        
    //     var currentIndex = variableNamePath.size() - 1;
    //     while (currentIndex > 0) {
    //         currentIndex -= 1;
    //         final Signature currentSignature = path.get(currentIndex).signature();
    //         currentProperty = ComplexProperty.create(variableNamePath.get(currentIndex), currentSignature, currentProperty);
    //     }

    //     return currentProperty;
    // }

}