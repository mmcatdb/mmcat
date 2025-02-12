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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Creates a transformation that computes new values based on the given {@link Computation}.
 * Then it filters the input list (or one of its inner properties) based on the computed values.
 */
public class ResultStructureComputer {

    // We could make this nullable, instead of using null tform in the output record.
    // However, it's better to always have a valid operation (that is sometimes a no-op) than to mix null and non-null values.
    // E.g., we might want to add something like logging later, which would be inpossible for nulls.

    public static ComputationTForm run(ResultStructure inputStructure, Computation computation, boolean isFilter) {
        final var outputStructure = inputStructure.copy();
        return new ResultStructureComputer(outputStructure).run(computation, isFilter);
    }

    /** The copy of the original structure. It might change during the algorithm because as we go throught new arguments, we might need to resolve them first. */
    private final ResultStructure outputStructure;
    /** Includes all computations that needs to be evaluated first. Then there is the final computation (if needed) and filter (also if needed). */
    private final List<TformRoot> outputTforms = new ArrayList<>();

    private ResultStructureComputer(ResultStructure inputStructure) {
        this.outputStructure = inputStructure.copy();
    }

    public record ComputationTForm(List<TformRoot> tforms, ResultStructure outputStructure) {

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

    private ComputationTForm run(Computation computation, boolean isFilter) {
        var referenceNode = findReferenceNodeForComparison(computation);
        if (referenceNode == null)
            // This corresponds to a constant filter, e.g., FILTER(1 = 1). In this case, we will filter the root.
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

        return new ComputationTForm(outputTforms, outputStructure);
    }

    /**
     * Finds the reference node (in the ResultStructure) to this expression.
     * Returns null if there is none (e.g., for a constant).
     */
    private @Nullable ResultStructure findReferenceNode(Expression expression) {
        if (expression instanceof Constant)
            // Constants don't have reference nodes - they can be used anywhere.
            return null;

        if (expression instanceof Variable variable) {
            // For variables, the reference node is always their parent.
            // Now, you might be thinking: "But what if the variable is the root? Gotcha! I see you didn't pay attention to the documentation!". Because the root variable can't be a leaf, so it isn't a simple value, so it can't be used in a computation. Checkmate.
            final ResultStructure node = outputStructure.tryFindDescendantByVariable(variable);
            if (node == null)
                throw new RuntimeException("Variable \"" + variable + "\" not found in result structure");

            return node.parent();
        }

        // Now it's starting to get interesting - different operators behave differently.
        final var computation = (Computation) expression;
        final var operator = computation.operator;

        if (operator.isSet())
            // Right now, we only support constant sets. So, this is the same case as for constants.
            return null;

        // There are three rules:
        //  1. The reference node of an expression is as low in the tree as possible.
        //  2. If it isn't an aggregation, there must be a 1:1 path from the reference node to each argument of its expression.
        //  3. A reference node of an aggregation has to have a 1:n path to its first argument.

        if (operator.isAggregation())
            return findReferenceNodeForAggregation(computation);

        return findReferenceNodeForComparison(computation);
    }

    private ResultStructure findReferenceNodeForAggregation(Computation computation) {
        // An aggregation might specify a second argument (a variable) which should be used as a reference node.
        if (computation.arguments.size() > 1) {
            // TODO Check the 3rd rule.
            final var variable = (Variable) computation.arguments.get(1);
            return findReferenceNode(variable);
        }

        // If there is no second argument, we start with the reference node of the first argument and then work our way up.
        ResultStructure output = findReferenceNode(computation.arguments.get(0));
        if (output == null || output.parent() == null)
            throw new RuntimeException("Aggregation without a reference node: " + computation);

        output = output.parent();

        while (!output.isArray) {
            output = output.parent();
            if (output == null)
                throw new RuntimeException("Aggregation without a reference node: " + computation);
        }

        return output;
    }

    private @Nullable ResultStructure findReferenceNodeForComparison(Computation computation) {
        // We start by finding the reference nodes of both arguments.
        final var lhs = findReferenceNode(computation.arguments.get(0));
        final var rhs = findReferenceNode(computation.arguments.get(1));

        if (lhs == rhs)
            // If both are null, a null is returned. In most cases, this wouldn't make sense. However, filters like FILTER(1 = 1) might be useful sometimes.
            return lhs;
        if (lhs == null)
            return rhs;
        if (rhs == null)
            return lhs;

        // Both nodes are different and not null. The reference node will be on the path from one to the other.
        // All edges are oriented - from parent to child (by default), or the other way around (for arrays). The path from reference node to both lhs and rhs can't go against the edge direction (2nd rule).
        // Therefore, there must be at most one viable reference node! (Just draw the arrows and you'll see.)
        final var path = GraphUtils.findPath(lhs, rhs);

        @Nullable ResultStructure output = null;

        for (final var structure : path.sourceToRoot()) {
            final var isParentArray = structure.parent().isArray;

            if (output == null) {
                // Reference node not found yet - we try to move as far as possible against the edge direction.
                if (isParentArray)
                    // We can't move any further, so we have to stop here. However, we continue the algorithm to make sure that both paths are valid.
                    output = structure;
            }
            else {
                if (!isParentArray)
                    // Reference node is found, so we have to move with the edge direction. But we can't, so that's an error.
                    throw new RuntimeException("No reference node found for comparison: " + computation);
            }
        }

        for (final var structure : path.rootToTarget()) {
            final var parent = structure.parent();

            if (output == null) {
                // We still need to go against the edge direction, but now the isArray flag is inverted.
                if (!parent.isArray)
                    output = parent;
            }
            else {
                if (parent.isArray)
                    // Again, the same but inverted.
                    throw new RuntimeException("No reference node found for comparison: " + computation);
            }
        }

        if (output == null)
            // We tried so hard and got so far, but in the end, it doesn't even matter.
            output = rhs;

        return output;
    }

    private TformRoot createComputationTform(Computation computation, ResultStructure reference) {
        final var output = new TformRoot();
        TformStep current = output;
        current = current.addChild(new TraverseList());

        if (reference != outputStructure) {
            // We are not computing the root, so we have to traverse to the reference node first.
            final var rootToReference = GraphUtils.findPath(outputStructure, reference);
            current = ResultStructureTformer.addPathSteps(current, rootToReference);
        }

        current = current.addChild(new AddToMap(computation.identifier()));
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

        current.addChild(new AddToOutput<LeafResult>());
    }

    private void resolveComputationArgument(TformStep current, ResultStructure computationReference, Computation argument) {
        // If the argument already exists, nothing is done.
        // We need to use this weird input-output structure passing here, because we need the inner algorithm to modify the same structure as we are using.
        final var childTform = new ResultStructureComputer(outputStructure).run(argument, false);
        outputTforms.addAll(childTform.tforms);

        final var argumentReference = findReferenceNode(argument);
        if (argumentReference != computationReference) {
            final var path = GraphUtils.findPath(computationReference, argumentReference);
            current = ResultStructureTformer.addPathSteps(current, path);
        }

        current = current.addChild(new TraverseMap(argument.identifier()));
        current.addChild(new AddToOutput<LeafResult>());
    }

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

        // The last step of the path is the actual reference value.
        keysToValue.add(computation.identifier());

        // The TraverseList it's also a remover, so we can use the filter step to remove the filtered node from the input.
        current.addChild(new FilterNode(keysToValue));

        return output;
    }

    private ResultStructure findFilteredNode(ResultStructure reference) {
        // There must be an 1:n (or 1:1) path from the reference node to the filtered node.
        // E.g., if User has Name, there is 1:n path from Name to User, so we can filter User by Name.
        // However, if User has Order, and there is n:1 path from Order to User, we can't filter User by Order.
        // So, we go as far up as possible, until we find a node that has a 1:n path to the reference node (or the root).
        ResultStructure output = reference;
        while (output.parent() != null && !output.parent().isArray)
            output = output.parent();

        return output;
    }

}
