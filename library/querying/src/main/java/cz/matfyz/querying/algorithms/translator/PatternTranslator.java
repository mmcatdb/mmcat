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
