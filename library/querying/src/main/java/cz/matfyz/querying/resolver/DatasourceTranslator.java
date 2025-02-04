package cz.matfyz.querying.resolver;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AbstractWrapperContext;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Expression.FunctionExpression;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternTree;
import cz.matfyz.querying.core.querytree.DatasourceNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

/*
 * This class translates the query part of a specific DatasourceNode.
 */
public class DatasourceTranslator {

    public static QueryStatement run(QueryContext context, DatasourceNode datasourceNode) {
        return new DatasourceTranslator(context, datasourceNode).run();
    }

    private final QueryContext context;
    private final DatasourceNode datasourceNode;
    private AbstractQueryWrapper wrapper;
    private DatasourceContext wrapperContext;

    public DatasourceTranslator(QueryContext context, DatasourceNode datasourceNode) {
        this.context = context;
        this.datasourceNode = datasourceNode;
    }

    private QueryStatement run() {
        this.wrapper = context.getProvider().getControlWrapper(datasourceNode.datasource).getQueryWrapper();

        wrapperContext = new DatasourceContext(context, datasourceNode.rootVariable);

        datasourceNode.kinds.forEach(this::processKind);
        datasourceNode.joinCandidates.forEach(this::processJoinCandidate);

        wrapper.setContext(wrapperContext);

        for (final FunctionExpression filter : datasourceNode.filters)
            processFilter(filter);

        return this.wrapper.createDSLStatement();
    }

    private void processFilter(FunctionExpression filter) {
        final var operator = filter.operator();

        if (operator.isComparison()) {
            final var left = createProperty(filter.arguments().get(0));
            final var rhs = filter.arguments().get(1);

            if (rhs instanceof Constant constant)
                wrapper.addFilter(left, constant, operator);
            else
                wrapper.addFilter(left, createProperty(rhs), operator);
            return;
        }

        if (operator.isSet()) {
            final var property = createProperty(filter.arguments().get(0));
            final var values = filter.arguments().stream().skip(1).map(expression -> (Constant) expression).toList();
            wrapper.addFilter(property, values, operator);
            return;
        }

        throw new UnsupportedOperationException("Unsupported filter operator: " + operator + ".");
    }

    private Property createProperty(Expression expression) {
        if (expression instanceof Variable variable) {
            /*
            // TODO: is the retyping to Variable needed? This can be applied to any term (at least type-wise).
            final var ctx = context.getContext(variable);
            final var mappings = ctx.mappings();
            final var signatures = ctx.signatures();

            if (signatures.size() != 1) {
                throw new UnsupportedOperationException("Cannot choose between multiple possible signatures.");
            }

            return new Property(mappings.get(0), signatures.get(0), null);
            */
            return wrapperContext.getProperty(variable);
        }

        if (expression instanceof FunctionExpression functionExpression) {
            // FIXME expressions (with possible aggregations)
            // final var aggregation = term.asFunctionExpression();
            // final var property = createProperty(aggregation.variable());
            // final var root = findAggregationRoot(property.mapping, property.path);

            // return new PropertyWithAggregation(property.mapping, property.path, null, root, aggregation.operator());
        }

        throw new UnsupportedOperationException("Can't create property from term: " + expression.getClass().getSimpleName() + ".");
    }

    private Signature findAggregationRoot(Mapping kind, Signature path) {
        // TODO
        throw new UnsupportedOperationException("DatasourceTranslator.findAggregationRoot not implemented.");
    }

    // PATTERN TRANSLATOR STUFF FROM HERE (TODO: remove)

    private record StackItem(
        PatternTree object,
        /** The closest parent property that has to be preserved in the property tree. */
        @Nullable Property preservedParent,
        /** Path from the `parent` to the currently processed `object`. If the parent is null, the path is from the root of the kind instead. */
        Signature pathFromParent
    ) {}

    private PatternForKind pattern;
    private Set<PatternTree> preservedObjects;
    private Deque<StackItem> stack;

    private void processKind(PatternForKind kind) {
        this.pattern = kind;
        preservedObjects = findPreservedObjects(kind.root);
        stack = new ArrayDeque<>();

        stack.push(new StackItem(kind.root, null, Signature.createEmpty()));
        while (!stack.isEmpty())
            processStackItem(stack.pop());
    }

    private void processStackItem(StackItem item) {
        if (!item.object.isTerminal()) {
            processInnerItem(item);
            return;
        }

        final var objectProperty = wrapperContext.createProperty(pattern.kind, item);

        // TODO isOptional is not supported yet.
        final var structure = wrapperContext.findOrCreateStructure(objectProperty);
        wrapper.addProjection(objectProperty, structure, false);
    }

    private void processInnerItem(StackItem item) {
        Property preservedParent;
        Signature pathFromParent;

        final var isNewParent = preservedObjects.contains(item.object);
        if (isNewParent) {
            preservedParent = wrapperContext.createProperty(pattern.kind, item);
            pathFromParent = Signature.createEmpty();
        }
        else {
            preservedParent = item.preservedParent;
            pathFromParent = item.pathFromParent;
        }

        for (final var child : item.object.children()) {
            final var childItem = new StackItem(
                child,
                preservedParent,
                pathFromParent.concatenate(child.signatureFromParent())
            );
            stack.push(childItem);
        }
    }

    private static class DatasourceContext implements AbstractWrapperContext {

        private final QueryContext context;
        private ResultStructure rootStructure;

        DatasourceContext(QueryContext context, Variable rootVariable) {
            this.context = context;
            rootStructure = new ResultStructure(rootVariable.name(), true, rootVariable);
        }

        private final Map<Property, Variable> propertyToVariable = new TreeMap<>();
        private final Map<Variable, Property> variableToProperty = new TreeMap<>();
        private final Map<Property, ResultStructure> propertyToStructure = new TreeMap<>();
        private final Map<ResultStructure, Property> structureToProperty = new TreeMap<>();

        Property createProperty(Mapping kind, StackItem item) {
            final var property = new Property(kind, item.pathFromParent, item.preservedParent);
            propertyToVariable.put(property, item.object.variable);
            variableToProperty.put(item.object.variable, property);

            return property;
        }

        ResultStructure findOrCreateStructure(@Nullable Property property) {
            if (property == null)
                return rootStructure;

            if (property.path.isEmpty())
                return findOrCreateStructure(property.parent);

            final var found = propertyToStructure.get(property);
            if (found != null)
                return found;

            final var isArray = property.path.hasDual();
            final Variable variable = propertyToVariable.get(property);
            final var structure = new ResultStructure(variable.name(), isArray, variable);

            propertyToStructure.put(property, structure);
            structureToProperty.put(structure, property);

            final var parent = findOrCreateStructure(property.parent);
            parent.addChild(structure, property.path);

            return structure;
        }

        @Override public ResultStructure rootStructure() {
            return rootStructure;
        }

        @Override public Property getProperty(ResultStructure structure) {
            return structureToProperty.get(structure);
        }

        private Property getProperty(Variable variable) {
            return variableToProperty.get(variable);
        }
    }


    /**
     * Finds all nodes that should be preserved in the property tree. Root is ommited because it's always preserved. The leaves as well. So only the child nodes of array edges with multiple preserved leaves are preserved.
     * Also finds all nodes specified as variables by the user - these should be preserved by default.
     */
    private Set<PatternTree> findPreservedObjects(PatternTree root) {
        // We start in the root. Whenever we find an object with multiple children, we add the last child of an array edge to the output.
        final Set<PatternTree> output = new TreeSet<>();
        final var rootObject = new PreservedStackObject(root, null);

        GraphUtils.forEachDFS(rootObject, stackObject -> {
            final var object = stackObject.object;
            final var lastChildOfArray = object.isChildOfArray() ? object : stackObject.lastChildOfArray;

            // All original objects are added.
            if (object.variable.isOriginal())
                output.add(object);

            // If the object has multiple children, the last child of array has to be preserved (if it isn't null ofc).
            if (object.children().size() > 1 && lastChildOfArray != null)
                output.add(lastChildOfArray);

            return object.children().stream()
                .map(child -> new PreservedStackObject(child, lastChildOfArray))
                .toList();
        });

        return output;
    }

    private record PreservedStackObject(PatternTree object, @Nullable PatternTree lastChildOfArray) {}

    private void processJoinCandidate(JoinCandidate candidate) {
        // // TODO
        // final Property from = createProperty(null);
        // // TODO
        // final Property to = createProperty(null);
        wrapper.addJoin(candidate.from().kind, candidate.to().kind, candidate.condition(), candidate.recursion(), candidate.isOptional());
    }

}
