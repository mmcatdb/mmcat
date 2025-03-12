package cz.matfyz.querying.resolver.queryresult;

import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.querying.resolver.queryresult.TformStep.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Creates a transformation that computes new values based on the given {@link Computation}.
 * Then it filters the input list (or one of its inner properties) based on the computed values.
 */
public class ResultStructureComputer {

    // We could make this nullable, instead of using null tform in the output record.
    // However, it's better to always have a valid operation (that is sometimes a no-op) than to mix null and non-null values.
    // E.g., we might want to add something like logging later, which would be inpossible for nulls.

    public static ComputationTform run(ResultStructure inputStructure, Computation computation, boolean isFilter) {
        final var outputStructure = inputStructure.copy();
        return new ResultStructureComputer(outputStructure).run(computation, isFilter);
    }

    /** The copy of the original structure. It might change during the algorithm because as we go throught new arguments, we might need to resolve them first. */
    private final ResultStructure outputStructure;
    /** Includes all computations that needs to be evaluated first. Then there is the final computation (if needed) and filter (also if needed). */
    private final List<TformRoot> outputTforms = new ArrayList<>();
    private final ReferenceNodeFinder referenceNodeFinder;

    private ResultStructureComputer(ResultStructure inputStructure) {
        this.outputStructure = inputStructure.copy();
        referenceNodeFinder = new ReferenceNodeFinder(outputStructure);
    }

    public record ComputationTform(List<TformRoot> tforms, ResultStructure outputStructure) {

        public QueryResult apply(ListResult input) {
            // Nothing changes here, we just apply the tforms.
            for (final var tform : tforms) {
                final var context = new TformContext(input);
                tform.apply(context);
            }

            return new QueryResult(input, outputStructure);
        }

        @Override public String toString() {
            final var builder = new StringBuilder();

            for (final var tform : tforms)
                builder.append(tform).append("\n");

            return builder.toString();
        }

    }

    private ComputationTform run(Computation computation, boolean isFilter) {
        var referenceNode = referenceNodeFinder.find(computation);
        if (referenceNode == null)
            // This corresponds to a constant filter, e.g., FILTER(1 = 1). In this case, we will filter the root.
            // FIXME Replace constant expressions by constants.
            referenceNode = outputStructure;

        if (!referenceNode.hasComputation(computation)) {
            // The computation is not yet resolved, so we have to do it first.
            final var tform = createComputationTform(computation, referenceNode);
            outputTforms.add(tform);
            outputStructure.addComputation(computation);
        }

        if (isFilter) {
            // It's a filter, so we have to create a filter tform.
            final var tform = createFilterTform(computation, referenceNode);
            outputTforms.add(tform);
        }

        return new ComputationTform(outputTforms, outputStructure);
    }

    // Computations

    private TformRoot createComputationTform(Computation computation, ResultStructure reference) {
        final var output = new TformRoot();
        TformStep current = output;
        current = current.addChild(new TraverseList());

        if (reference != outputStructure) {
            // We are not computing the root, so we have to traverse to the reference node first.
            final var rootToReference = GraphUtils.findPath(outputStructure, reference);
            current = ResultStructureTformer.addPathSteps(current, rootToReference);
        }

        final TformStep resolver = current.addChild(new ResolveComputation(computation));

        final List<Expression> arguments = computation.operator.isAggregation()
            // Only the first argument of aggregations represent real values. The second is here just to change the default reference node.
            ? computation.arguments.subList(0, 1)
            : computation.arguments;

        for (final var argument : arguments)
            resolveArgument(resolver, reference, argument);

        return output;
    }

    private void resolveArgument(TformStep parent, ResultStructure computationReference, Expression argument) {
        // All arguments will be written to a list. The list will be then used as an input for the computation.
        final TformStep current = parent.addChild(new WriteToList<LeafResult>());

        if (argument instanceof Constant constant)
            current.addChild(new CreateLeaf(constant.value()));
        else if (argument instanceof Variable variable)
            resolveVariableArgument(current, computationReference, variable);
        else
            resolveComputationArgument(current, computationReference, (Computation) argument);
    }

    private void resolveVariableArgument(TformStep current, ResultStructure computationReference, Variable argument) {
        // We can't use the reference node here because the reference node for a variable is its parent. So we need to traverse from the parent to the variable and this is just easier.
        final ResultStructure variableNode = outputStructure.tryFindDescendantByVariable(argument);
        if (variableNode != computationReference) {
            final var path = GraphUtils.findPath(computationReference, variableNode);
            current = ResultStructureTformer.addPathSteps(current, path);
        }

        current.addChild(new AddToOutput());
    }

    private void resolveComputationArgument(TformStep current, ResultStructure computationReference, Computation argument) {
        // If the argument already exists, nothing is done.
        // We need to use this weird input-output structure passing here, because we need the inner algorithm to modify the same structure as we are using.
        final var childTform = new ResultStructureComputer(outputStructure).run(argument, false);
        outputTforms.addAll(childTform.tforms);

        var argumentReference = referenceNodeFinder.find(argument);
        if (argumentReference == null)
            // The argument is a constant expression, so it's reference node is the root.
            // FIXME Replace constant expressions by constants.
            argumentReference = outputStructure;

        if (argumentReference != computationReference) {
            final var path = GraphUtils.findPath(computationReference, argumentReference);
            current = ResultStructureTformer.addPathSteps(current, path);
        }

        current.addChild(new RecallComputation(argument));
    }

    // Filtering

    private TformRoot createFilterTform(Computation computation, ResultStructure reference) {
        final ResultStructure filtered = findFilteredNode(reference);

        final var output = new TformRoot();
        TformStep current = output;
        current = current.addChild(new TraverseList());

        if (filtered != outputStructure) {
            // We are not filtering the root, so we have to traverse to the filtered node first.
            final var rootToFiltered = GraphUtils.findDirectPath(outputStructure, filtered);
            current = ResultStructureTformer.addDirectPathSteps(current, rootToFiltered);
        }

        // Now we are at the filtered node. The last step is TraverseList, which puts each list children to the input (one by one).
        // We know that the reference must be either the filtered or one of its descendants. Moreover, there must be a 1:1 path from the reference to the filtered. So we can traverse multiple maps at once.
        final List<String> keysToValue = new ArrayList<>();
        if (reference != filtered) {
            final var filteredToReference = GraphUtils.findDirectPath(filtered, reference);
            filteredToReference.stream().map(structure -> structure.name)
                .forEach(keysToValue::add);
        }

        // The TraverseList it's also a remover, so we can use the filter step to remove the filtered node from the input.
        current.addChild(new FilterNode(keysToValue, computation));

        return output;
    }

    private ResultStructure findFilteredNode(ResultStructure reference) {
        // There must be an 1:n (or 1:1) path from the reference node to the filtered node.
        // E.g., if User has Name, there is 1:n path from Name to User, so we can filter User by Name.
        // However, if User has Order, and there is n:1 path from Order to User, we can't filter User by Order.
        // So, we go as far up as possible, until we find a node that has a 1:n path to the reference node (or the root).
        ResultStructure output = reference;
        while (output.parent() != null && !output.isArray)
            output = output.parent();

        return output;
    }

    /**
     * Finds the reference node (in the ResultStructure) to this expression.
     * Returns null if there is none (e.g., for a constant).
     */
    static class ReferenceNodeFinder {

        private final ResultStructure root;
        // Why java has to be like this? Why we can't reimplement the Comparable interface for multiple subclasses? My disappointment is immeasurable and my day is ruined.
        private final Map<Computation, @Nullable ResultStructure> cache = new TreeMap<>();

        public ReferenceNodeFinder(ResultStructure root) {
            this.root = root;
        }

        public @Nullable ResultStructure find(Expression expression) {
            if (expression instanceof Constant)
                // Constants don't have reference nodes - they can be used anywhere.
                return null;

            if (expression instanceof Variable variable)
                return findReferenceNodeForVariable(variable);

            // Now it's starting to get interesting - different operators behave differently.
            final var computation = (Computation) expression;
            final var operator = computation.operator;

            if (operator.isAggregation())
                // The reference node of an aggregation is the whole querry (which doesn't make much sense), or one of the arguments of the GROUP BY clause. In either case, it's not handled here.
                throw new UnsupportedOperationException("Reference nodes of aggregations are handled separately.");

            // We have to do the double check here because the cache might contain null values (which are valid).
            if (cache.containsKey(computation))
                return cache.get(computation);

            final var output = findReferenceNodeForArguments(computation.arguments);
            cache.put(computation, output);
            return output;
        }

        private ResultStructure findReferenceNodeForVariable(Variable variable) {
            // For variables, the reference node is always their corresponding node.
            final ResultStructure node = root.tryFindDescendantByVariable(variable);
            if (node == null)
                throw new RuntimeException("Variable \"" + variable + "\" not found in result structure");

            return node;
        }

        private @Nullable ResultStructure findReferenceNodeForArguments(List<Expression> arguments) {
            // There is only one rule: there must be a 1:1 (or n:1) path from the reference node to each argument of its expression.
            // However, in order to make the reference node unambiguous, we want it to be as close to the arguments as possible.
            //  - If the expression uses only one variable, it is the reference node.
            //  - If it uses multiple variables, it is the one closest to them (see comments below what exactly that means).
            //  - If none variables are used (e.g., `FILTER(1 > 0)`, for whatever reason), it's null.

            // We start by finding the reference nodes of all arguments.
            Set<ResultStructure> references = new TreeSet<>();
            for (final var agument : arguments) {
                final var reference = find(agument);
                if (reference != null)
                    references.add(reference);
            }

            if (references.isEmpty())
                // There are no reference nodes, so we can't find a common one.
                // In most cases, this wouldn't make sense. However, filters like FILTER(1 = 1) might be useful sometimes.
                return null;

            if (references.size() == 1)
                // There is only one reference node, so it's the common one.
                return references.iterator().next();

            // There are multiple different argument reference nodes.
            // Some edges are oriented - if the child is array, there is 1:n path from the parent to the child, so we can't go this way from the reference node to it's aguments (2nd rule).
            // However, each child has only one parent, so we can always go from child to parent.
            // In order for the reference node to be the closest one, we will take only those that are on the path from one argument to another.
            // This ensures that if there are multiple viable reference nodes, there must be a 1:1 path between each other. (Just draw the arrows and you'll see.)
            // If there are still mutliple options, we will choose the one that is highest in the tree.
            // It should be easy to prove that there is only one such node (if there is any), and that it doesn't depend on the order of the arguments.

            final var iterator = references.iterator();
            ResultStructure ans = iterator.next();

            while (iterator.hasNext()) {
                final var next = iterator.next();
                // After the first iteration, there are a 1:1 paths from ans to both nodes that were compared (if ans isn't null ofc).
                // After the second iteration, there will be a 1:1 paths from the new ans to both original ans and the new node. However, if we have a -> b and b -> c, then a -> c, so there will be 1:1 paths from the new ans to all previously compared nodes. By induction, ans will be the common reference node.
                ans = tryFindCommonReferenceNodeForPair(ans, next);
                if (ans == null)
                    // There is no common reference node. That's obviously an error.
                    throw new RuntimeException("No reference node found for arguments: " + arguments);
            }

            return ans;
        }

        private static @Nullable ResultStructure tryFindCommonReferenceNodeForPair(ResultStructure a, ResultStructure b) {
            final var path = GraphUtils.findPath(a, b);

            // We should start at `a` and got to the common root. We have to stop at the first array we find, because we can always go against the array from the child to the parent, but not the other way around.
            @Nullable ResultStructure aBottomArray = null;
            for (final var structure : path.sourceToRoot()) {
                if (structure.isArray) {
                    aBottomArray = structure;
                    break;
                }
            }

            // Now we do the same thing, but on the other path. But the path is reversed, so we don't stop at the first array.
            @Nullable ResultStructure bBottomArray = null;
            for (final var structure : path.rootToTarget()) {
                if (structure.isArray)
                    bBottomArray = structure;
            }

            if (aBottomArray == null) {
                if (bBottomArray != null)
                    return bBottomArray;

                // All nodes can be the reference node, so we choose the highest one - the common root.
                final var aTop = path.sourceToRoot().size() == 0 ? a : path.sourceToRoot().getLast();
                return aTop.parent();
            }

            if (bBottomArray == null)
                return aBottomArray;

            // We tried so hard and got so far, but in the end, it doesn't even matter.
            return null;
        }

    }

}
