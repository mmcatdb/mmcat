package cz.matfyz.querying.resolver.queryresult;

import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.MapResult;
import cz.matfyz.core.querying.ResultNode;
import cz.matfyz.core.utils.printable.*;
import cz.matfyz.querying.resolver.queryresult.TformContext.RemoverContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents one basic step in transforming a query result (ResultNode) to another one (with different ResultStructure).
 * It can be either a traverse (to a parent, a map or a list), a creation of a leaf, a creation of a map or a list, or a write to a map or a list.
 * The steps usually have children which are also traversed. Some steps spawn multiple new "branches" which are traversed one by one. Therefore, the whole transformation can be obtained by simply appliing the first step to the root node.
 *
 * This system is designed to transform the data as fast as possible. We only have to create the steps once and then we can apply them to any amount of data.
 */
public abstract class TformStep implements Printable {

    private final List<TformStep> children = new ArrayList<>();

    /**
     * Adds a child to this step. Then returns the child.
     * @param child A child step
     * @return The child step
     */
    public TformStep addChild(TformStep child) {
        if (!isChildrenSupported())
            throw new UnsupportedOperationException("This step doesn't support children.");
        if (child instanceof TformRoot)
            throw new UnsupportedOperationException("Root step can't be nested.");

        children.add(child);
        return child;
    }

    /**
     * Root is sometimes used as a placeholder or a collection of other steps.
     * This helps us to remove all unnecessary root steps (except for the first one).
     */
    public void addChildOrRoot(TformStep child) {
        if (!isChildrenSupported())
            throw new UnsupportedOperationException("This step doesn't support children.");
        if (child instanceof TformRoot)
            children.addAll(child.children);

        children.add(child);
    }

    protected boolean isChildrenSupported() {
        return true;
    }

    public abstract void apply(TformContext context);

    protected void applyChildren(TformContext context) {
        for (final var child : children)
            child.apply(context);
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    protected void printChildren(Printer printer) {
        if (children.isEmpty()) {
            printer.nextLine();
            return;
        }

        if (children.size() == 1) {
            printer.nextLine().append("    ").append(children.get(0));
            return;
        }

        printer.append(":").down().nextLine();
        for (final var child : children)
            printer.append("--- ").append(child).nextLine();
        printer.remove().up();
    }

    protected boolean useIndexed = false;

    /** Use the indexed inputs instead of the default ones. Doesn't work for all steps tho. */
    public TformStep indexed() {
        useIndexed = true;
        return this;
    }

    // Steps

    public static class TformRoot extends TformStep {
        @Override public void apply(TformContext context) {
            applyChildren(context);
        }

        @Override public void printTo(Printer printer) {
            printer.append("root");
            printChildren(printer);
        }
    }

    static class TraverseParent extends TformStep {
        @Override public void apply(TformContext context) {
            final var lastInput = context.inputs.pop();
            applyChildren(context);
            context.inputs.push(lastInput);
        }

        @Override public void printTo(Printer printer) {
            printer.append("parent.traverse");
            printChildren(printer);
        }
    }

    static class TraverseMap extends TformStep {
        private final String key;

        TraverseMap(String key) {
            this.key = key;
        }

        @Override public void apply(TformContext context) {
            final var stack = useIndexed ? context.indexed : context.inputs;

            final var currentMap = (MapResult) stack.peek();
            stack.push(currentMap.children().get(key));
            applyChildren(context);
            stack.pop();
        }

        @Override public void printTo(Printer printer) {
            printer
                .append(useIndexed ? "map.traverseIndexed" : "map.traverse")
                .append("(")
                .append(key)
                .append(")");
            printChildren(printer);
        }
    }

    static class TraverseList extends TformStep implements RemoverContext {
        private int i;
        private List<Integer> removed = new ArrayList<>();

        @Override public void apply(TformContext context) {
            final var stack = useIndexed ? context.indexed : context.inputs;

            final var currentList = (ListResult) stack.peek();
            final var children = currentList.children();

            context.removers.push(this);

            for (i = 0; i < children.size(); i++) {
                stack.push(children.get(i));
                applyChildren(context);
                stack.pop();
            }

            context.removers.pop();
            currentList.removeChildren(removed);
        }

        @Override public void getRemoved() {
            removed.add(i);
        }

        @Override public void printTo(Printer printer) {
            printer.append(useIndexed ? "list.traverseIndexed" : "list.traverse");
            printChildren(printer);
        }
    }

    /**
     * Peeks the input and writes it directly to the output, thus effectively copying the last value.
     * However, it isn't a copy so be careful with steps that directly modify the results.
     * Except for the leaves ofc, since they are immutable.
     */
    static class AddToOutput extends TformStep {
        @Override public void apply(TformContext context) {
            final var stack = useIndexed ? context.indexed : context.inputs;

            final var inputNode = stack.peek();
            context.outputs.push(inputNode);
        }

        @Override public void printTo(Printer printer) {
            printer
                .append(useIndexed ? "output.addIndexed" : "output.add")
                .nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

    static class CreateLeaf extends TformStep {
        private final String value;

        CreateLeaf(String value) {
            this.value = value;
        }

        @Override public void apply(TformContext context) {
            context.outputs.push(new LeafResult(value));
        }

        @Override public void printTo(Printer printer) {
            printer
                .append("leaf.create(").append(value).append(")")
                .nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

    // The expected structure is this:
    // CreateMap -> WriteToMap with k1 -> TraverseMap with k1 -> ... possible other traverses ... -> ... children that creates the new map items
    //           -> WriteToMap with k2 -> TraverseMap with k2 -> ... possible other traverses ... -> ... children that creates the new map items
    //           -> ... the same with other keys ...
    static class CreateMap extends TformStep {
        @Override public void apply(TformContext context) {
            final var builder = new MapResult.Builder();
            context.builders.push(builder);
            applyChildren(context);
            context.builders.pop();
            context.outputs.push(builder.build());
        }

        @Override public void printTo(Printer printer) {
            printer.append("map.create");
            printChildren(printer);
        }
    }

    static class WriteToMap extends TformStep {
        private final String key;

        WriteToMap(String key) {
            this.key = key;
        }

        @Override public void apply(TformContext context) {
            applyChildren(context);
            final ResultNode outputNode = context.outputs.pop();
            final var builder = (MapResult.Builder) context.builders.peek();
            builder.put(key, outputNode);
        }

        @Override public void printTo(Printer printer) {
            printer.append("map.write(").append(key).append(")");
            printChildren(printer);
        }
    }

    // The expected structure is this:
    // CreateList -> TraverseList -> ... possible other traverses ... -> WriteToList -> ... children that creates the new list items.
    static class CreateList<T extends ResultNode> extends TformStep {
        @Override public void apply(TformContext context) {
            final var builder = new ListResult.Builder<T>();
            context.builders.push(builder);
            applyChildren(context);
            context.builders.pop();
            context.outputs.push(builder.build());
        }

        @Override public void printTo(Printer printer) {
            printer.append("list.create");
            printChildren(printer);
        }
    }

    static class WriteToList<T extends ResultNode> extends TformStep {
        @SuppressWarnings("unchecked")
        @Override public void apply(TformContext context) {
            applyChildren(context);
            final var outputNode = (T) context.outputs.pop();
            final var builder = (ListResult.Builder<T>) context.builders.peek();
            builder.add(outputNode);
        }

        @Override public void printTo(Printer printer) {
            printer.append("list.write");
            printChildren(printer);
        }
    }

    /**
     * A partial implementation of WriteToIndex required for a dependent join.
     */
    static class WriteToSet extends TformStep {
        // This step can be divided to smaller steps (traversing + writing to index), but why if it's gona always be used this way?
        // An exception to the traversal rule might be if we wanted to travel the parent. But that's not possible right now.

        private final Set<String> set;
        private final List<String> pathToIdentifier;

        WriteToSet(Set<String> set, List<String> pathToIdentifier) {
            this.set = set;
            this.pathToIdentifier = pathToIdentifier;
        }

        @Override public void apply(TformContext context) {
            final var thisNode = context.inputs.peek();
            final var identifierLeaf = (LeafResult) traversePath(thisNode, pathToIdentifier);

            set.add(identifierLeaf.value);
        }

        @Override public void printTo(Printer printer) {
            printer.append("set.write(");

            for (int i = 0; i < pathToIdentifier.size(); i++)
                printer.append(pathToIdentifier.get(i)).append(".");

            if (pathToIdentifier.isEmpty())
                printer.append("#empty");
            else
                printer.remove();

            printer
                .append(")")
                .nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

    /**
     * Peeks node from input and writes it to the index using the identifier as a key.
     * The path to the identifier must be 1:1 so we always travel through maps. There is no need for any other kind of traversal.
     */
    static class WriteToIndex<T extends ResultNode> extends TformStep {
        // This step can be divided to smaller steps (traversing + writing to index), but why if it's gona always be used this way?
        // An exception to the traversal rule might be if we wanted to travel the parent. But that's not possible right now.
        // EDIT FROM THE FUTURE: For WriteToSet it might be useful to split so that code isn't duplicated... TODO

        private final Map<String, T> index;
        private final List<String> pathToIdentifier;

        WriteToIndex(Map<String, T> index, List<String> pathToIdentifier) {
            this.index = index;
            this.pathToIdentifier = pathToIdentifier;
        }

        @SuppressWarnings("unchecked")
        @Override public void apply(TformContext context) {
            final var thisNode = (T) context.inputs.peek();
            final var identifierLeaf = (LeafResult) traversePath(thisNode, pathToIdentifier);

            index.put(identifierLeaf.value, thisNode);
        }

        @Override public void printTo(Printer printer) {
            printer.append("index.write(");

            for (int i = 0; i < pathToIdentifier.size(); i++)
                printer.append(pathToIdentifier.get(i)).append(".");

            if (pathToIdentifier.isEmpty())
                printer.append("#empty");
            else
                printer.remove();

            printer
                .append(")")
                .nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

    /**
     * Retrieves a node from the index and puts it on the indexed stack. The path to the identifier is read from the current input.
     * If a value isn't found in the index, the input node is removed.
     */
    static class TraverseIndex extends TformStep {
        private final Map<String, MapResult> index;
        private final List<String> pathToIdentifier;

        TraverseIndex(Map<String, MapResult> index, List<String> pathToIdentifier) {
            this.index = index;
            this.pathToIdentifier = pathToIdentifier;
        }

        @Override public void apply(TformContext context) {
            final var targetMap = (MapResult) context.inputs.peek();
            final var identifierLeaf = (LeafResult) traversePath(targetMap, pathToIdentifier);
            final var indexedMap = (MapResult) index.get(identifierLeaf.value);

            if (indexedMap == null) {
                context.removers.peek().getRemoved();
                return;
            }

            context.indexed.push(indexedMap);
            applyChildren(context);
            context.indexed.pop();
        }

        @Override public void printTo(Printer printer) {
            printer.append("index.traverse(");

            for (int i = 0; i < pathToIdentifier.size(); i++)
                printer.append(pathToIdentifier.get(i)).append(".");

            if (pathToIdentifier.isEmpty())
                printer.append("#empty");
            else
                printer.remove();

            printer.append(")");
            printChildren(printer);
        }
    }

    // This function can be probably divided to two - reading from index and writing to map. Then it can be even more generalized - we can write one key at a time, therefore we don't have to use the list.
    // However, there is a problem with complexity. Currently, it isn't clear which arguments are input and which output. E.g., the writeToMap takes output and writes it into the builder. However, in merging, we might want to:
    //  - read a node from the index and write it to the input map,
    //  - read a map from the index and write the output node to id.
    // This gets out of hands very quickly. The previous transformation steps work because they all behave in a similar way (create structure - traverse children - write to builder). But this isn't the case here. Maybe we should create only specific functions - like this one, so that we can ensure that output of one is the input of the next one. Or use more "linear" approach - i.e., a list of steps instead of a deeply nested structure.
    // However, we are still probably going to switch to some other structure soon, so ...

    static class MergeToMap extends TformStep {
        private final Map<String, MapResult> index;
        private final List<String> pathToIdentifier;

        private @Nullable List<String> keys = null;
        private @Nullable String selfKey = null;

        private MergeToMap(Map<String, MapResult> index, List<String> pathToIdentifier) {
            this.index = index;
            this.pathToIdentifier = pathToIdentifier;
        }

        /**
         * Merges all keys from the source map (from index) to the target map (from input).
         */
        public static MergeToMap keys(Map<String, MapResult> index, List<String> pathToIdentifier, List<String> keys) {
            final var output = new MergeToMap(index, pathToIdentifier);
            output.keys = keys;
            return output;
        }

        /**
         * Merges the whole source map (from index) to the target map (from input).
         */
        public static MergeToMap self(Map<String, MapResult> index, List<String> pathToIdentifier, String selfKey) {
            final var output = new MergeToMap(index, pathToIdentifier);
            output.selfKey = selfKey;
            return output;
        }

        @Override public void apply(TformContext context) {
            final var targetMap = (MapResult) context.inputs.peek();
            final var identifierLeaf = (LeafResult) traversePath(targetMap, pathToIdentifier);
            final var sourceMap = (MapResult) index.get(identifierLeaf.value);

            if (sourceMap == null) {
                context.removers.peek().getRemoved();
                return;
            }

            if (selfKey != null) {
                targetMap.children().put(selfKey, sourceMap);
            }
            else {
                for (final String key : keys)
                    targetMap.children().put(key, sourceMap.children().get(key));
            }
        }

        @Override public void printTo(Printer printer) {
            printer.append("map.merge(");

            for (int i = 0; i < pathToIdentifier.size(); i++)
                printer.append(pathToIdentifier.get(i)).append(".");

            if (pathToIdentifier.isEmpty())
                printer.append("#empty");
            else
                printer.remove();

            printer.append(", ");

            if (selfKey != null) {
                printer.append(selfKey);
            }
            else {
                printer.append("[ ");
                for (final String key : keys)
                    printer.append(key).append(", ");

                printer.remove().append(" ]");
            }

            printer
                .append(")")
                .nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

    /**
     * Consumes output and adds it to a map from the input (under the specified key).
     */
    static class AddToMap extends TformStep {
        private final String key;

        AddToMap(String key) {
            this.key = key;
        }

        @Override public void apply(TformContext context) {
            applyChildren(context);
            final ResultNode outputNode = context.outputs.pop();
            final var currentMap = (MapResult) context.inputs.peek();
            currentMap.children().put(key, outputNode);
        }

        @Override public void printTo(Printer printer) {
            printer.append("map.add(").append(key).append(")");
            printChildren(printer);
        }
    }

    // Note - this can be used to remove the whole subtree. Also, in combination with a traversal, we can remove any subtree from the result tree.
    static class RemoveFromMap extends TformStep {
        private final String key;

        RemoveFromMap(String key) {
            this.key = key;
        }

        @Override public void apply(TformContext context) {
            final var currentMap = (MapResult) context.inputs.peek();
            currentMap.children().remove(key);
        }

        @Override public void printTo(Printer printer) {
            printer
                .append("map.remove(").append(key).append(")")
                .nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

    // This can be its own step, but it is not necessary. It is just a helper for now.

    /** Traverses a continuous path of maps. They have to exist and they have to be maps! */
    private static ResultNode traversePath(ResultNode input, List<String> path) {
        ResultNode current = input;
        for (final String key : path)
            current = ((MapResult) current).children().get(key);

        return current;
    }

    /** All argumets of the computation are expected to be resolved already. Their values should be provided in the child steps. */
    static class ResolveComputation extends TformStep {
        private final Computation computation;

        ResolveComputation(Computation computation) {
            this.computation = computation;
        }

        @Override public void apply(TformContext context) {
            // Collect all arguments with values that we need for the computation.
            final var argumentsBuilder = new ListResult.Builder<LeafResult>();
            context.builders.push(argumentsBuilder);
            applyChildren(context);
            context.builders.pop();

            final List<String> arguments = argumentsBuilder.build().children().stream()
                .map(leaf -> ((LeafResult) leaf).value)
                .toList();

            final String result = computation.resolve(arguments);
            context.inputs.peek().setComputedValue(computation, result);
        }

        @Override public void printTo(Printer printer) {
            printer.append("computation.resolve(").append(computation.identifier()).append(": ").append(computation).append(")");
            printChildren(printer);
        }
    }

    /**
     * Peeks the input, finds a computation result here and writes it directly to the output (as a new {@link LeafResult}).
     */
    static class RecallComputation extends TformStep {
        private final Computation computation;

        RecallComputation(Computation computation) {
            this.computation = computation;
        }

        @Override public void apply(TformContext context) {
            final var inputNode = context.inputs.peek();
            final String value = inputNode.getComputedValue(computation);
            context.outputs.push(new LeafResult(value));
        }

        @Override public void printTo(Printer printer) {
            printer
                .append("computation.recall(").append(computation.identifier()).append(")")
                .nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

    /**
     * Takes a node from the input, then finds a second node according to the specified path. If its value isn't "true", the first node is removed.
     * If the computation is specified, the second node can be any {@link ResultNode}. The value is taken from its computationResults.
     * Otherwise, the second node must be a {@link LeafResult} and its value is used directly.
     * Must be run inside a remover context.
     * Both nodes might be the same picture (just ask the corporate to tell the difference).
     */
    static class FilterNode extends TformStep {
        private final List<String> pathToValue;
        private final @Nullable Computation computation;

        FilterNode(List<String> pathToValue, @Nullable Computation computation) {
            this.pathToValue = pathToValue;
            this.computation = computation;
        }

        @Override public void apply(TformContext context) {
            final var filteredNode = context.inputs.peek();
            final var valueNode = traversePath(filteredNode, pathToValue);
            final String value = computation != null
                ? valueNode.getComputedValue(computation)
                : ((LeafResult) valueNode).value;

            if (!value.equals("true"))
                context.removers.peek().getRemoved();
        }

        @Override public void printTo(Printer printer) {
            printer.append("node.filter(");
            for (final var key : pathToValue)
                printer.append(key).append(".");
            printer.remove().append(")").nextLine();
        }

        @Override protected boolean isChildrenSupported() {
            return false;
        }
    }

}
