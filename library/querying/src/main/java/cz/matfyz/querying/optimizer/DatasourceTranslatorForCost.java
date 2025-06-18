package cz.matfyz.querying.optimizer;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AbstractWrapperContext;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternTree;
import cz.matfyz.querying.core.querytree.DatasourceNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class translates the query part of a specific DatasourceNode.
 * TODO if this is useless then remove it too
 */
public class DatasourceTranslatorForCost {



    public record Projection(Property property, ResultStructure structure, boolean isOptional) {}
    public record ProjectionsAndContext(ArrayList<Projection> projections, DatasourceContext wrapperContext) {}



    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceTranslatorForCost.class);

    public static ProjectionsAndContext run(QueryContext context, DatasourceNode datasourceNode) {
        return new DatasourceTranslatorForCost(context, datasourceNode).run();
    }

    private final QueryContext context;
    private final DatasourceNode datasourceNode;
    private AbstractQueryWrapper wrapper;
    private DatasourceContext wrapperContext;

    private final ArrayList<Projection> projections = new ArrayList<>();

    public DatasourceTranslatorForCost(QueryContext context, DatasourceNode datasourceNode) {
        this.context = context;
        this.datasourceNode = datasourceNode;
    }

    private ProjectionsAndContext run() {
        this.wrapper = context.getProvider().getControlWrapper(datasourceNode.datasource).getQueryWrapper();

        wrapperContext = new DatasourceContext(context, datasourceNode.rootVariable);

        datasourceNode.kinds.forEach(this::processKind);
        datasourceNode.joinCandidates.forEach(this::processJoinCandidate);

        wrapper.setContext(wrapperContext);

        for (final Computation filter : datasourceNode.filters)
            processFilter(filter);

        return new ProjectionsAndContext(projections, wrapperContext);
    }

    private void processFilter(Computation filter) {
        final var operator = filter.operator;

        if (operator.isComparison()) {
            final var left = createProperty(filter.arguments.get(0));
            final var rhs = filter.arguments.get(1);

            if (rhs instanceof Constant constant)
                wrapper.addFilter(left, constant, operator);
            else
                wrapper.addFilter(left, createProperty(rhs), operator);
            return;
        }

        if (operator.isSet()) {
            final var property = createProperty(filter.arguments.get(0));
            final var values = filter.arguments.stream().skip(1).map(expression -> (Constant) expression).toList();
            wrapper.addFilter(property, values, operator);
            return;
        }

        throw new UnsupportedOperationException("Unsupported filter operator: " + operator + ".");
    }

    private Property createProperty(Expression expression) {
        if (expression instanceof Variable variable) {

            return wrapperContext.getProperty(variable);
        }

        if (expression instanceof Computation computation) {
            // FIXME expressions (with possible aggregations)
            // final var aggregation = term.asComputation();
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

    private record StackItem(
        PatternTree node,
        /** The closest parent property that has to be preserved in the property tree. */
        @Nullable Property preservedParent,
        /** Path from the `parent` to the currently processed `node`. If the parent is null, the path is from the root of the kind instead. */
        Signature pathFromParent
    ) {}

    private PatternForKind pattern;
    private Set<PatternTree> preservedNodes;
    private Deque<StackItem> stack;

    private void processKind(PatternForKind kind) {
        this.pattern = kind;
        preservedNodes = findPreservedNodes(kind.root);
        // TODO This is just a temporary fix.
        final Set<Signature> availablePaths = new TreeSet<>();
        availablePaths.add(Signature.createEmpty());
        addAllSubpathSignatures(availablePaths, kind.kind.accessPath(), Signature.createEmpty());

        preservedNodes = preservedNodes.stream()
            .filter(node -> availablePaths.contains(node.computePathFromRoot()))
            .collect(Collectors.toSet());

        stack = new ArrayDeque<>();

        stack.push(new StackItem(kind.root, null, Signature.createEmpty()));
        while (!stack.isEmpty())
            processStackItem(stack.pop());
    }

    // TODO This is just a temporary fix.
    private static void addAllSubpathSignatures(Set<Signature> output, ComplexProperty accessPath, Signature current) {
        accessPath.subpaths().forEach(subpath -> {
            final var next = current.concatenate(subpath.signature());
            output.add(next);

            if (subpath instanceof ComplexProperty complexSubpath)
                addAllSubpathSignatures(output, complexSubpath, next);
        });
    }

    private void processStackItem(StackItem item) {
        LOGGER.debug("processStackItem:\n{}", item);
        if (!item.node.isTerminal()) {
            processInnerItem(item);
            return;
        }

        final var node = wrapperContext.createProperty(pattern.kind, item);

        // TODO isOptional is not supported yet.
        final var structure = wrapperContext.findOrCreateStructure(node);
        wrapper.addProjection(node, structure, false);
        projections.add(new Projection(node, structure, false));

        LOGGER.debug("addProjection:\n{}\n{}", node, structure);
    }

    private void processInnerItem(StackItem item) {
        Property preservedParent;
        Signature pathFromParent;

        final var isNewParent = preservedNodes.contains(item.node);
        if (isNewParent) {
            preservedParent = wrapperContext.createProperty(pattern.kind, item);
            pathFromParent = Signature.createEmpty();
        }
        else {
            preservedParent = item.preservedParent;
            pathFromParent = item.pathFromParent;
        }

        LOGGER.debug("preservedParent:\n{}", preservedParent);
        LOGGER.debug("pathFromParent:\n{}", pathFromParent);

        for (final var child : item.node.children()) {
            final var childItem = new StackItem(
                child,
                preservedParent,
                pathFromParent.concatenate(child.signatureFromParent())
            );
            stack.push(childItem);
        }
    }

    public static class DatasourceContext implements AbstractWrapperContext {

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
            propertyToVariable.put(property, item.node.variable);
            variableToProperty.put(item.node.variable, property);

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

        public Property getProperty(Variable variable) {
            return variableToProperty.get(variable);
        }
    }

    /**
     * Finds all nodes that should be preserved in the property tree. Root is ommited because it's always preserved. The leaves as well. So only the child nodes of array edges with multiple preserved leaves are explicitly preserved.
     * Also finds all nodes specified as variables by the user - these should be preserved by default.
     */
    private static Set<PatternTree> findPreservedNodes(PatternTree root) {
        // We start in the root. Whenever we find an node with multiple children, we add the last child of an array edge to the output.
        final Set<PatternTree> output = new TreeSet<>();
        final var rootNode = new PreservedStackNode(root, null);

        GraphUtils.forEachDFS(rootNode, stackNode -> {
            final var node = stackNode.node;
            final var lastChildOfArray = node.isChildOfArray() ? node : stackNode.lastChildOfArray;

            // All original nodes are added.
            if (node.variable.isOriginal())
                output.add(node);

            // If the node has multiple children, the last child of array has to be preserved (if it isn't null ofc).
            if (node.children().size() > 1 && lastChildOfArray != null)
                output.add(lastChildOfArray);

            return node.children().stream()
                .map(child -> new PreservedStackNode(child, lastChildOfArray))
                .toList();
        });

        return output;
    }

    private record PreservedStackNode(PatternTree node, @Nullable PatternTree lastChildOfArray) {}

    private void processJoinCandidate(JoinCandidate candidate) {
        final var fromPath = candidate.from().getPatternTree(candidate.variable()).computePathFromRoot();
        final var toPath = candidate.to().getPatternTree(candidate.variable()).computePathFromRoot();

        wrapper.addJoin(candidate.from().kind, candidate.to().kind, fromPath, toPath, candidate.recursion(), candidate.isOptional());
    }

}
