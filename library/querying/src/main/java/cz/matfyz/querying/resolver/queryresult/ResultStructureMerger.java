package cz.matfyz.querying.resolver.queryresult;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.MapResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.resolver.queryresult.TformStep.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Creates a transformation that merges a query result corresponding to the source structure to a query result corresponding to the target structure.
 * The structures are matched by the values of the match variable.
 * The source will be merged to a position in the target that corresponds to the source's root variable (join structure).
 * There must be 1:1 cardinality between the match structure and the sourceRoot (resp. match and the join structure).
 */
public class ResultStructureMerger {

    /**
     * @param sourceRoot The root of the source structure.
     * @param targetRoot The root of the target structure.
     * @param matchVar The variable that will be used to matchVar the source and target structures.
     * @return A transformation that merges the source structure to the target structure.
     */
    public static MergeTform run(QueryContext context, ResultStructure sourceRoot, ResultStructure targetRoot, Variable matchVar) {
        return new ResultStructureMerger(context, sourceRoot, targetRoot, matchVar).run();
    }

    // TODO This still isn't perfect. If some variables are missing from one of the structures because of composite signatures, the merge might not work.
    // It's fixed for the join structure, but not for its children. It should work similarly to how we handle the join structure, but it's probably going to be at least twice the amount of pain in the ass to make it work.

    private final QueryContext context;
    private final ResultStructure sourceRoot;
    private final ResultStructure targetRoot;
    private final Variable matchVar;

    private ResultStructureMerger(QueryContext context, ResultStructure sourceRoot, ResultStructure targetRoot, Variable matchVar) {
        this.context = context;
        this.sourceRoot = sourceRoot;
        // We don't want to mutate the original structure.
        this.targetRoot = targetRoot.copy();
        this.matchVar = matchVar;
    }

    public record MergeTform(TformRoot sourceTform, TformRoot targetTform, TformRoot targetIdsTform, ResultStructure newStructure, Map<String, MapResult> index, Set<String> targetIds) {

        public QueryResult apply(ListResult source, ListResult target) {
            applySource(source);
            return applyTarget(target);
        }

        /** Creates the index. */
        public void applySource(ListResult source) {
            sourceTform.apply(new TformContext(source));
        }

        /** Returns ids of source data. Requires calling applySource() first. */
        public Set<String> getSourceIds() {
            return index.keySet();
        }

        /** Returns ids of target data. */
        public Set<String> getTargetIds(ListResult target) {
            targetIdsTform.apply(new TformContext(target));
            return targetIds;
        }

        /** Merges the source to the target and remove the target identifiers, then merges with source. */
        public QueryResult applyTarget(ListResult target) {
            targetTform.apply(new TformContext(target));

            return new QueryResult(target, newStructure);
        }
    }

    private MergeTform run() {
        final Map<String, MapResult> index = new TreeMap<>();
        final Set<String> targetIds = new HashSet<>();

        final var sourceTform = createSourceTform(index);

        // needs to be done before modifying target ResultStructure
        final var targetIdsTform = createTargetIdsTform(targetIds);

        final var targetTform = createTargetTform(index);

        return new MergeTform(sourceTform, targetTform, targetIdsTform, targetRoot, index, targetIds);
    }

    private TformRoot createSourceTform(Map<String, MapResult> index) {
        final var tform = new TformRoot();
        TformStep current = tform;

        final ResultStructure match = sourceRoot.findDescendantByVariable(matchVar);
        final var rootToMatch = GraphUtils.findDirectPath(sourceRoot, match);

        current = current.addChild(new TraverseList());
        current.addChild(new WriteToIndex<MapResult>(index, pathToKeys(rootToMatch)));

        return tform;
    }

    private TformRoot createTargetTform(Map<String, MapResult> index) {
        final var targetTform = new TformRoot();
        // First, we iterate over all target values.
        TformStep current = targetTform.addChild(new TraverseList());

        // Make sure the join structure exists and traverse to it.
        final var record = traverseToJoin(current);
        current = record.current;
        final var join = record.join;

        final var match = targetRoot.findDescendantByVariable(matchVar);
        final var joinToMatch = GraphUtils.findDirectPath(join, match);

        // And only after that we load the corresponding values from the source. The reason is that the join might be in an array inside the target.
        current = current.addChild(new TraverseIndex(index, pathToKeys(joinToMatch)));

        final var mergeTform = merge(sourceRoot, join);
        current.addChildOrRoot(mergeTform);

        return targetTform;
    }

    private TformRoot createTargetIdsTform(Set<String> targetIds) {
        // (same as in createTargetTform)
        final var targetTform = new TformRoot();
        TformStep current = targetTform.addChild(new TraverseList());

        // final var record = traverseToJoin(current);
        // current = record.current;
        // final var join = record.join;

        // final var match = targetRoot.findDescendantByVariable(matchVar);
        // final var joinToMatch = GraphUtils.findDirectPath(join, match);

        final var match = targetRoot.findDescendantByVariable(matchVar);
        final var rootToMatch = GraphUtils.findDirectPath(targetRoot, match);

        current = current.addChild(new WriteToSet(targetIds, pathToKeys(rootToMatch)));

        return targetTform;
    }

    record TraverseToJoin(TformStep current, ResultStructure join) {}

    private TraverseToJoin traverseToJoin(TformStep current) {
        final var joinVar = sourceRoot.variable;

        @Nullable ResultStructure join = targetRoot.tryFindDescendantByVariable(joinVar);
        if (join != null) {
            if (join != targetRoot) {
                // Just a traverse to the join structure if necessary.
                current = ResultStructureTformer.addDirectPathSteps(current, GraphUtils.findDirectPath(targetRoot, join));
            }

            return new TraverseToJoin(current, join);
        }

        // The join structure might not be there yet. Or, there might be a composite signature that we have to split.
        // Either way, we have to create the join structure.
        // This little maneuver is going to cost us 51 years ...

        final var rootVariableTree = GraphUtils.findBFS(context.variables, tree -> tree.variable.equals(targetRoot.variable));
        final var joinVariableTree = GraphUtils.findBFS(context.variables, tree -> tree.variable.equals(joinVar));
        final var path = GraphUtils.findPath(rootVariableTree, joinVariableTree);

        // We find a path from the root to the join structure (consisting of only basic signatures).
        final var rootToJoin = Signature.concatenate(
            path.sourceToRoot().stream().map(tree -> tree.edgeFromParent.dual()),
            path.rootToTarget().stream().map(tree -> tree.edgeFromParent)
        );

        // The structure just above the join structure.
        final var parent = targetRoot.traverseSignature(rootToJoin);
        final var rootToParent = parent.getSignatureFromRoot();
        final var parentToJoin = rootToJoin.cutPrefix(rootToParent);

        // We know the parent now so we can traverse there.
        current = ResultStructureTformer.addDirectPathSteps(current, GraphUtils.findDirectPath(targetRoot, parent));

        // Now we have to figure out whether the join structure doesn't exist (and we have to create it) or it's there but it's a composite signature.
        final var presentSignature = parent.childSignatures().stream().filter(s -> s.hasPrefix(parentToJoin)).findFirst();

        if (presentSignature.isEmpty()) {
            // It can't be array because there must be a 1:1 path from join to match (and we know that match is not a child of join, because join doesn't exist yet).
            join = parent.addChild(new ResultStructure(joinVar.name(), false, joinVar), parentToJoin);

            // We don't extend the current because this should be resolved before all other steps.
            current
                .addChild(new AddToMap(join.name))
                .addChild(new CreateMap());
        }
        else {
            // The join is supposed to be between the parent and the child. So we have to split the child.
            final var parentToChild = presentSignature.get();
            final var joinToChild = parentToChild.cutPrefix(parentToJoin);

            final var child = parent.removeChild(parentToChild);

            // If the child isn't an array, neither should be the join.
            // However, if the child is an array, there are two possibilities:
            // - The match is a descendant of the child. So join has to be an array because the child can't be anymore.
            // - The match is not a descendant of the child. So join can't be an array.
            final var isMatchInJoin = child.tryFindDescendantByVariable(matchVar) != null;
            final var isJoinArray = child.isArray && isMatchInJoin;
            join = parent.addChild(new ResultStructure(joinVar.name(), isJoinArray, joinVar), parentToJoin);
            final var newChild = isJoinArray ? child.copy(false) : child;
            join.addChild(newChild, joinToChild);

            // Again, all these steps need to be resolved before the other steps.
            var current2 = current;
            current2 = current2
                .addChild(new AddToMap(join.name))
                .addChild(new TraverseMap(child.name));

            if (isJoinArray) {
                current2 = current2
                    .addChild(new CreateList<MapResult>())
                    .addChild(new TraverseList())
                    .addChild(new WriteToList<MapResult>());
            }

            current2
                .addChild(new CreateMap())
                .addChild(new WriteToMap(child.name))
                .addChild(new AddToOutput());

            current.addChild(new RemoveFromMap(child.name));
        }

        current = current.addChild(new TraverseMap(join.name));

        return new TraverseToJoin(current, join);
    }

    // Merging result structures
    // A node from source should be inserted to target. There are three options:
    // The node doesn't exist in target - we copy it.
    // The node exists in the target - we call merge on all its children.
    // The node doesn't exist in target, but there is another node that shares a signature prefix - we should split them and then try again.
    //  - Probably not doing right now. Too much work.

    // Merging results
    // The source results are unique, their target counterparts don't have to be. Therefore, one source might be merged into multiple targets, so we have to copy the sources. We don't have to copy the targets, we can just modify them.
    // First, we create index over the sources. This is done in a separate tform.
    // The next tform works as follows:
    // 1. We iterate over the targets and traverse to the property.
    // 2. We obtain the value of the match variable and use it to recall the source.
    // 3. We iterate over all property keys (DFS) that need adjustment. The same traversing steps are done on the source as well.
    // 4. Once a subtree from source is required, we copy it (using standard create and write steps) and add it to the target.

    /**
     * Merges the source structure to the target structure. Both are expected to have the same variable.
     * @param isRoot If true, the final transformation will omit traversing to the structures.
     */
    private @Nullable TformStep merge(ResultStructure source, ResultStructure target) {
        final Set<Signature> childSignatures = new TreeSet<>();
        childSignatures.addAll(source.childSignatures());
        childSignatures.addAll(target.childSignatures());

        final List<TformStep> childTforms = new ArrayList<>();

        for (final Signature signature : childSignatures) {
            final var sourceChild = source.getChild(signature);
            final var targetChild = target.getChild(signature);

            if (sourceChild == null) {
                // No need to do anything since we are merging data to the target so everything on target stays the same (unless overwritten).
                continue;
            }
            else if (targetChild == null) {
                final var childTform = new AddToMap(sourceChild.name);
                final var copyTform = copySource(sourceChild);
                childTform.addChildOrRoot(copyTform);

                childTforms.add(childTform);

                target.addChild(sourceChild.copy(), signature);
            }
            else {
                if (targetChild.isArray)
                    throw new UnsupportedOperationException("Merging arrays is not supported yet.");
                final var mergeTform = merge(sourceChild, targetChild);
                if (mergeTform == null)
                    continue;

                final var childTform = new TraverseMap(targetChild.name);
                childTform
                    .addChild(new TraverseMap(sourceChild.name).indexed())
                    .addChildOrRoot(mergeTform);

                childTforms.add(childTform);
            }
        }

        if (childTforms.isEmpty())
            // There are either no children or all are already present in the target. No need to do anything.
            return null;

        final TformStep outputTform = new TformRoot();
        for (final var childTform : childTforms)
            outputTform.addChildOrRoot(childTform);

        return outputTform;
    }

    /**
     * Traverses the current indexed structure, copies it and writes the copy to the output.
     */
    private TformStep copySource(ResultStructure source) {
        final TformStep output = new TraverseMap(source.name).indexed();
        TformStep current = output;

        if (source.children().isEmpty()) {
            if (source.isArray) {
                current = current
                    .addChild(new CreateList<LeafResult>())
                    .addChild(new TraverseList().indexed())
                    .addChild(new WriteToList<LeafResult>());
            }

            // This is a leaf. We can just copy it to the output.
            current.addChild(new AddToOutput().indexed());
            return output;
        }

        if (source.isArray) {
            current = current
                .addChild(new CreateList<MapResult>())
                .addChild(new TraverseList().indexed())
                .addChild(new WriteToList<MapResult>());
        }

        // Now we have to recursively copy the whole structure. Since this is a map, we start with that.
        final var mapStep = current.addChild(new CreateMap());

        for (final var child : source.children()) {
            TformStep childCurrent = mapStep.addChild(new WriteToMap(child.name));
            childCurrent.addChild(copySource(child));
        }

        return output;
    }

    private List<String> pathToKeys(List<ResultStructure> path) {
        return path.stream()
            .map(node -> node.name)
            .toList();
    }

}
