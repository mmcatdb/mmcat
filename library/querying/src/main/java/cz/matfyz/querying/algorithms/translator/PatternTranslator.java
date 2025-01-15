package cz.matfyz.querying.algorithms.translator;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AbstractWrapperContext;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Constant;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternObject;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.parsing.Term;
import cz.matfyz.querying.parsing.Term.StringValue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

class PatternTranslator {

    public static void run(QueryContext context, DatasourceNode node, AbstractQueryWrapper wrapper) {
        new PatternTranslator(context, wrapper).run(node);
    }

    private final QueryContext context;
    private final AbstractQueryWrapper wrapper;

    private PatternTranslator(QueryContext context, AbstractQueryWrapper wrapper) {
        this.context = context;
        this.wrapper = wrapper;
    }

    private WrapperContext wrapperContext;

    private void run(DatasourceNode node) {
        wrapperContext = new WrapperContext(context, node.rootTerm);

        node.kinds.forEach(this::processKind);
        node.joinCandidates.forEach(this::processJoinCandidate);

        wrapper.setContext(wrapperContext);
    }

    private record StackItem(
        PatternObject object,
        /** The closest parent property that has to be preserved in the property tree. */
        @Nullable Property preservedParent,
        /** Path from the `parent` to the currently processed `object`. If the parent is null, the path is from the root of the kind instead. */
        Signature pathFromParent
    ) {}

    private PatternForKind pattern;
    private Set<PatternObject> preservedObjects;
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

        final Term term = item.object.term;
        final var objectProperty = wrapperContext.createProperty(pattern.kind, item);

        if (term instanceof StringValue constantObject)
            wrapper.addFilter(objectProperty, new Constant(List.of(constantObject.value())), ComparisonOperator.Equal);
        else {
            // TODO isOptional is not supported yet.
            final var structure = wrapperContext.findOrCreateStructure(objectProperty);
            wrapper.addProjection(objectProperty, structure, false);
        }
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

    private static class WrapperContext implements AbstractWrapperContext {

        private final QueryContext context;
        private QueryStructure rootStructure;

        WrapperContext(QueryContext context, Term rootTerm) {
            this.context = context;
            rootStructure = new QueryStructure(rootTerm.getIdentifier(), true, context.getObject(rootTerm));
        }

        private final Map<Property, Term> propertyToTerm = new TreeMap<>();
        private final Map<Property, QueryStructure> propertyToStructure = new TreeMap<>();
        private final Map<QueryStructure, Property> structureToProperty = new TreeMap<>();

        Property createProperty(Mapping kind, StackItem item) {
            final var property = new Property(kind, item.pathFromParent, item.preservedParent);
            propertyToTerm.put(property, item.object.term);

            return property;
        }

        QueryStructure findOrCreateStructure(@Nullable Property property) {
            if (property == null)
                return rootStructure;

            if (property.path.isEmpty())
                return findOrCreateStructure(property.parent);

            final var found = propertyToStructure.get(property);
            if (found != null)
                return found;

            final var isArray = property.path.hasDual();
            final Term term = propertyToTerm.get(property);
            final var structure = new QueryStructure(term.getIdentifier(), isArray, context.getObject(term));

            propertyToStructure.put(property, structure);
            structureToProperty.put(structure, property);

            final var parent = findOrCreateStructure(property.parent);
            parent.addChild(structure, property.path);

            return structure;
        }

        @Override public QueryStructure rootStructure() {
            return rootStructure;
        }

        @Override public Property getProperty(QueryStructure structure) {
            return structureToProperty.get(structure);
        }

    }


    /**
     * Finds all nodes that should be preserved in the property tree. Root is ommited because it's always preserved. The leaves as well. So only the child nodes of array edges with multiple preserved leaves are preserved.
     * Also finds all nodes specified as variables by the user - these should be preserved by default.
     */
    private Set<PatternObject> findPreservedObjects(PatternObject root) {
        // We start in the root. Whenever we find an object with multiple children, we add the last child of an array edge to the output.
        final Set<PatternObject> output = new TreeSet<>();
        final var rootObject = new PreservedStackObject(root, null);

        GraphUtils.forEachDFS(rootObject, stackObject -> {
            final var object = stackObject.object;
            final var lastChildOfArray = object.isChildOfArray() ? object : stackObject.lastChildOfArray;

            // All original objects are added.
            if (object.term.isOriginal())
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

    private record PreservedStackObject(PatternObject object, @Nullable PatternObject lastChildOfArray) {}

    private void processJoinCandidate(JoinCandidate candidate) {
        // // TODO
        // final Property from = createProperty(null);
        // // TODO
        // final Property to = createProperty(null);
        wrapper.addJoin(candidate.from().kind, candidate.to().kind, candidate.joinProperties(), candidate.recursion(), candidate.isOptional());
    }

}
