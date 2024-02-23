package cz.matfyz.querying.algorithms.translator;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Constant;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.patterntree.KindPattern;
import cz.matfyz.querying.core.patterntree.PatternObject;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.parsing.ParserNode.Term;
import cz.matfyz.querying.parsing.StringValue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

class PatternTranslator {

    public static void run(PatternNode pattern, AbstractQueryWrapper wrapper) {
        new PatternTranslator(wrapper).run(pattern);
    }

    private final AbstractQueryWrapper wrapper;

    private PatternTranslator(AbstractQueryWrapper wrapper) {
        this.wrapper = wrapper;
    }

    private void run(PatternNode pattern) {
        wrapper.defineRoot(pattern.rootTerm.getIdentifier());

        pattern.kinds.forEach(this::processKind);
        pattern.joinCandidates.forEach(this::processJoinCandidate);
    }

    private record StackItem(
        PatternObject object,
        /** The closest parent property that has to be preserved in the property tree. */
        @Nullable Property preservedParent,
        /** Path from the `parent` to the currently processed `object`. If the parent is null, the path is from the root of the kind instead. */
        Signature pathFromParent
    ) {}

    private KindPattern kind;
    private Set<PatternObject> preservedObjects;
    private Deque<StackItem> stack;

    private void processKind(KindPattern kind) {
        // TODO this is weird, because it's happening for each kind - meaning that the last one overrides all the previous ones
        // wrapper.defineRoot(kind.root.schemaObject, kind.root.term.getIdentifier());

        this.kind = kind;
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
        // This might be wrong, because we might need all of the kind's objects? Because we might need them for joins?
        // It should be possible to create a new term for each object.
        if (term == null)
            return;

        final var objectProperty = new Property(kind.kind, item.pathFromParent, item.preservedParent);

        if (term instanceof StringValue constantObject)
            wrapper.addFilter(objectProperty, new Constant(List.of(constantObject.value)), ComparisonOperator.Equal);
        else
            // TODO isOptional is not supported yet.
            wrapper.addProjection(objectProperty, term.getIdentifier(), false);
    }

    private void processInnerItem(StackItem item) {
        var preservedParent = item.preservedParent;
        var pathFromParent = item.pathFromParent;

        final var isNewParent = preservedObjects.contains(item.object);
        if (isNewParent) {
            preservedParent = new Property(kind.kind, pathFromParent, preservedParent);
            pathFromParent = Signature.createEmpty();
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

    /**
     * Finds all nodes that should be preserved in the property tree. Root is ommited because it's always preserved. The leaves as well (unless they don't have term). So only the child nodes of array edges with multiple preserved leaves are preserved.
     */
    private Set<PatternObject> findPreservedObjects(PatternObject root) {
        final Set<PatternObject> relevantObjects = findRelevantObjects(root);

        // We start in the root. Whenever we find an object with multiple relevant children, we add the last child of an array edge to the output.
        final Set<PatternObject> output = new TreeSet<>();
        final var rootObject = new PreservedStackObject(root, null);

        GraphUtils.forEachDFS(rootObject, stackObject -> {
            final var object = stackObject.object;
            final var lastChildOfArray = object.isChildOfArray() ? object : stackObject.lastChildOfArray;

            final var relevantChildren = object.children().stream()
                .filter(relevantObjects::contains)
                .map(child -> new PreservedStackObject(child, lastChildOfArray))
                .toList();

            // If the object has multiple relevant children, we add the last child of an array edge to the output (if it isn't null ofc).
            if (relevantChildren.size() > 1 && lastChildOfArray != null)
                output.add(lastChildOfArray);

            return relevantChildren;
        });

        return output;
    }

    /**
     * Finds all nodes that are to be used in the property tree. I.e., both the preserved nodes and nodes with terms.
     */
    private  Set<PatternObject> findRelevantObjects(PatternObject root) {
        final Set<PatternObject> output = new TreeSet<>();
        // A stack of nodes we need to check whether we can remove them. Originally, its the leaves of the tree.
        final Deque<PatternObject> objectsToCheck = new ArrayDeque<>();

        GraphUtils.forEachDFS(root, object -> {
            output.add(object);
            if (object.isTerminal())
                objectsToCheck.push(object);

            return object.children();
        });

        // Now we start with the leaves and recursively delete them if they don't have terms.
        while (!objectsToCheck.isEmpty()) {
            final var object = objectsToCheck.pop();
            if (isObjectRelevant(object, output))
                continue;

            // If the object isn't relevant, we remove it and (later) check its parent.
            output.remove(object);
            final var parent = object.parent();
            if (parent != null)
                objectsToCheck.push(parent);
        }

        return output;
    }

    private boolean isObjectRelevant(PatternObject object, Set<PatternObject> currentRelevantObjects) {
        // If the object is terminal and has no term, it's not relevant.
        if (object.isTerminal())
            return object.term != null;

        // A non-terminal object is relevant if it still has at least one relevant child.
        return object.children().stream().anyMatch(currentRelevantObjects::contains);
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
