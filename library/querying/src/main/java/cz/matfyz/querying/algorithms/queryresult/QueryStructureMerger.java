package cz.matfyz.querying.algorithms.queryresult;

import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.querying.queryresult.*;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.algorithms.queryresult.TformStep.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Creates a transformation that merges a query result corresponding to the source structure to a query result corresponding to the target structure on a position specified by the targetProperty.
 * The structures are matched by the properties of sourceMatch (from the root of the sourceRoot) and targetMatch (from the targetProperty).
 * The sourceMatch property is recursively deleted.
 * There must be at most a N:1 cardinality between the sourceMatch and the sourceRoot (resp. targetMatch and targetProperty).
 */
public class QueryStructureMerger {

    /**
     * @param sourceRoot The root of the source structure.
     * @param targetRoot The root of the target structure.
     * @param targetProperty The property (map) to which the source structure will be added as a direct child.
     * @param sourceMatch The child (not necessarily direct) of the sourceRoot that will be matched against the targetMatch.
     * @param targetMatch The child (not necessarily direct) of the targetProperty that will be matched against the sourceMatch.
     * @return A transformation that merges the source structure to the target structure.
     */
    public static MergeTform run(QueryStructure sourceRoot, QueryStructure targetRoot, QueryStructure targetProperty, QueryStructure sourceMatch, QueryStructure targetMatch) {
        return new QueryStructureMerger(sourceRoot, targetRoot).run(targetProperty, sourceMatch, targetMatch);
    }

    private final QueryStructure sourceRoot;
    private final QueryStructure targetRoot;

    private QueryStructureMerger(QueryStructure sourceRoot, QueryStructure targetRoot) {
        this.sourceRoot = sourceRoot;
        this.targetRoot = targetRoot;
    }

    public record MergeTform(TformRoot sourceTform, TformRoot targetTform, QueryStructure newStructure) {

        public QueryResult apply(ResultList source, ResultList target) {
            // First create the index.
            sourceTform.apply(new TformContext(source));

            // Then then merge the source to the target and remove the target identifiers.
            targetTform.apply(new TformContext(target));

            return new QueryResult(target, newStructure);
        }

    }

    private MergeTform run(QueryStructure targetProperty, QueryStructure sourceMatch, QueryStructure targetMatch) {
        final var rootToTarget = GraphUtils.findPath(targetRoot, targetProperty);
        final var sourceToMatch = GraphUtils.findPath(sourceRoot, sourceMatch).rootToTarget();
        final var targetToMatch = GraphUtils.findPath(targetProperty, targetMatch).rootToTarget();

        final var sourceTform = new TformRoot();
        TformStep current = sourceTform;

        // First, we iterate over the source property and create an index over it.
        current = current.addChild(new TraverseList());
        final Map<String, ResultMap> index = new TreeMap<>();
        final List<String> sourceToMatchKeys = fromRootPathToKeys(sourceToMatch);
        current.addChild(new WriteToIndex<ResultMap>(index, sourceToMatchKeys));

        // Then, we iterate over the target property.
        final var targetTform = new TformRoot();
        current = targetTform;
        current = current.addChild(new TraverseList());
        current = QueryStructureTformer.addPathSteps(current, rootToTarget);

        // We merge the source from the index to the target ...
        final List<String> targetToMatchKeys = fromRootPathToKeys(targetToMatch);
        current.addChild(MergeToMap.self(index, targetToMatchKeys, sourceRoot.name));

        // ... and delete the target match.
        final List<String> deleteKeys = findDeleteKeys(targetToMatch);
        for (int i = 0; i < deleteKeys.size() - 1; i++)
            current = current.addChild(new TraverseMap(deleteKeys.get(i)));
        current.addChild(new RemoveFromMap(deleteKeys.get(deleteKeys.size() - 1)));

        final var newStructure = createNewStructure(targetProperty, targetToMatch, sourceToMatch, deleteKeys);

        return new MergeTform(sourceTform, targetTform, newStructure);
    }

    // We ignore the sourceToRoot path because we are always traversing from the root. We also skip the first element because it's the root.
    private List<String> fromRootPathToKeys(List<QueryStructure> path) {
        return path.stream()
            .skip(1)
            .map(node -> node.name)
            .toList();
    }

    /** Returns the path of keys which should traversed from the target property. The last key in the list should be then deleted. */
    private List<String> findDeleteKeys(List<QueryStructure> targetToMatch) {
        // We just need to find the longest subtree that can be deleted. We need it's name and the path to it.
        String keyToDelete = targetToMatch.get(targetToMatch.size() - 1).name;

        // We traverse the path from the back until we find a node with more than one child. That one can't be deleted. The previous ones should be.
        // The last in the list is the target identifier itself, so we ignore it.
        for (int i = targetToMatch.size() - 2; i > 0; i--) {
            final var map = targetToMatch.get(i);
            if (map.children().size() > 1)
                break;

            // If the map has only one child (which will be deleted) so it's safe to delete it as well.
            keyToDelete = map.name;
        }

        final List<String> output = new ArrayList<>();

        // We skip the first element because we are already there - no need to traverse into it.
        for (int i = 1; i < targetToMatch.size(); i++) {
            final var map = targetToMatch.get(i);
            if (map.name.equals(keyToDelete))
                break;

            output.add(map.name);
        }

        output.add(keyToDelete);

        return output;
    }

    private QueryStructure createNewStructure(QueryStructure targetProperty, List<QueryStructure> targetToMatch, List<QueryStructure> sourceToMatch, List<String> deleteKeys) {
        final var output = targetRoot.copy();

        final Signature targetToMatchSignature = fromRootPathToSignature(targetToMatch);
        final Signature sourceToMatchSignature = fromRootPathToSignature(sourceToMatch);
        final Signature targetToSourceSignature = targetToMatchSignature.cutSuffix(sourceToMatchSignature);

        if (targetToSourceSignature == null)
            throw QueryException.message("The source structure is not a substructure of the target structure.");

        final var targetPropertyCopy = GraphUtils.findDFS(output, s -> s.equals(targetProperty));
        // The source is not a root so it's not gonna be an array.
        final var sourceRootCopy = sourceRoot.copy(false);
        targetPropertyCopy.addChild(sourceRootCopy, targetToSourceSignature);

        QueryStructure current = targetPropertyCopy;
        for (int i = 0; i < deleteKeys.size() - 1; i++)
            current = current.getChild(deleteKeys.get(i));

        current.removeChild(deleteKeys.get(deleteKeys.size() - 1));

        return output;
    }

    private Signature fromRootPathToSignature(List<QueryStructure> path) {
        final var signatures = path.stream().skip(1).map(node -> node.signatureFromParent).toList();
        return Signature.concatenate(signatures);
    }

}
