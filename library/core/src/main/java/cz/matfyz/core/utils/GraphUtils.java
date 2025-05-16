package cz.matfyz.core.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class GraphUtils {

    public interface Edge<N extends Comparable<N>> {
        N from();
        N to();
    }

    public record Component<N extends Comparable<N>, E extends Edge<N>>(
        int id,
        Set<N> nodes,
        List<E> edges
    ) implements Comparable<Component<N, E>> {
        @Override public int compareTo(Component<N, E> other) {
            return id - other.id;
        }
    }

    public static <N extends Comparable<N>, E extends Edge<N>> Collection<Component<N, E>> findComponents(Collection<E> edges) {
        final var finder = new ComponentFinder<N, E>();
        edges.forEach(finder::addEdge);

        return finder.getComponents();
    }

    public static <N extends Comparable<N>, E extends Edge<N>> Collection<N> findRoots(Component<N, E> component) {
        final var nodes = new TreeSet<>(component.nodes);
        component.edges.forEach(e -> nodes.remove(e.to()));

        return nodes;
    }

    private static class ComponentFinder<N extends Comparable<N>, E extends Edge<N>> {

        private Map<N, Component<N, E>> components = new TreeMap<>();
        private int nextId = 0;

        public void addEdge(E edge) {
            final N a = edge.from();
            final N b = edge.to();

            final var aComponent = components.get(a);
            var bComponent = components.get(b);

            if (aComponent == null) {
                if (bComponent == null) {
                    bComponent = new Component<N, E>(nextId++, new TreeSet<>(), new ArrayList<>());
                    bComponent.nodes.add(b);
                    components.put(b, bComponent);
                }

                handleOneNull(edge, bComponent, a);
                return;
            }

            if (bComponent == null) {
                handleOneNull(edge, aComponent, b);
                return;
            }

            if (aComponent.nodes.size() > bComponent.nodes.size())
                mergeComponents(edge, aComponent, bComponent);
            else
                mergeComponents(edge, bComponent, aComponent);
        }

        private void handleOneNull(E edge, Component<N, E> xComponent, N y) {
            xComponent.nodes.add(y);
            xComponent.edges.add(edge);
            components.put(y, xComponent);
        }

        private void mergeComponents(E edge, Component<N, E> bigger, Component<N, E> smaller) {
            bigger.nodes.addAll(smaller.nodes);
            bigger.edges.addAll(smaller.edges);
            bigger.edges.add(edge);
            smaller.nodes.forEach(x -> components.put(x, bigger));
        }

        public Set<Component<N, E>> getComponents() {
            return new TreeSet<>(components.values());
        }

    }

    public interface TopDownTree<T extends TopDownTree<T>> {

        Collection<T> children();

    }

    public interface Tree<T extends Tree<T>> extends TopDownTree<T>, Comparable<T> {

        @Nullable T parent();

    }

    public record TreePath<T extends Tree<T>>(
        /** Source is the first element, root is not included. If the source is the root, the path is empty. */
        List<T> sourceToRoot,
        /** Root is not included, target is the last element. If the target is the root, the path is empty. */
        List<T> rootToTarget
    ) {}

    /** Finds a path from the source to the common root and then back to the target. */
    public static <T extends Tree<T>> TreePath<T> findPath(T source, T target) {
        assert target != null;

        if (source.equals(target))
            return new TreePath<T>(List.of(), List.of());

        final var sourceToRoot = computePathToRoot(source);
        final var targetToRoot = computePathToRoot(target);

        int sourceIndex = sourceToRoot.size() - 1;
        int targetIndex = targetToRoot.size() - 1;

        while (
            sourceIndex >= 0 &&
            targetIndex >= 0 &&
            sourceToRoot.get(sourceIndex).equals(targetToRoot.get(targetIndex))
        ) {
            sourceIndex--;
            targetIndex--;
        }

        final var sourceToRootTrimmed = sourceToRoot.subList(0, sourceIndex + 1);
        final var targetToRootTrimmed = targetToRoot.subList(0, targetIndex + 1);

        return new TreePath<>(sourceToRootTrimmed, targetToRootTrimmed.reversed());
    }

    private static <T extends Tree<T>> List<T> computePathToRoot(T node) {
        assert node != null;

        final var output = new ArrayList<T>();
        T current = node;

        while (current != null) {
            output.add(current);
            current = current.parent();
        }

        return output;
    }

    /**
     * Finds a path from the source to the target. The target must be a direct descendant of the source!
     * @return The source is not included, the target is the last element. If the source is the target, the path is empty.
     */
    public static <T extends Tree<T>> List<T> findDirectPath(T source, T target) {
        assert target != null;

        final List<T> targetToSource = new ArrayList<>();

        T current = target;

        while (current != source) {
            targetToSource.add(current);
            current = current.parent();
        }

        return targetToSource.reversed();
    }

    public static <T extends TopDownTree<T>> @Nullable T findDFS(T tree, Predicate<T> predicate) {
        assert tree != null;

        Deque<T> stack = new ArrayDeque<>();
        stack.push(tree);

        while (!stack.isEmpty()) {
            final var current = stack.pop();

            if (predicate.test(current))
                return current;

            current.children().forEach(stack::push);
        }

        return null;
    }

    public static <T extends TopDownTree<T>> @Nullable T findBFS(T tree, Predicate<T> predicate) {
        assert tree != null;

        Queue<T> queue = new ArrayDeque<>();
        queue.add(tree);

        while (!queue.isEmpty()) {
            final var current = queue.poll();

            if (predicate.test(current))
                return current;

            current.children().forEach(queue::add);
        }

        return null;
    }

    public static <T extends Tree<T>> T findSubroot(T root, Collection<T> nodes) {
        assert root != null;

        return new TreeSubrootFinder<T>(root).findSubroot(nodes);
    }

    private static class TreeSubrootFinder<T extends Tree<T>> {

        private Map<T, Integer> depths = new TreeMap<>();

        TreeSubrootFinder(T root) {
            fillDepths(root, 0);
        }

        private void fillDepths(T node, int currentDepth) {
            depths.put(node, currentDepth);

            for (final var child : node.children())
                fillDepths(child, currentDepth + 1);
        }

        public T findSubroot(Collection<T> nodes) {
            int maximalDepth = Integer.MAX_VALUE;
            for (final var node : nodes)
                maximalDepth = Math.min(maximalDepth, depths.get(node));

            Set<T> nodesAtCurrentDepth = new TreeSet<>();
            for (var node : nodes) {
                for (int i = depths.get(node); i > maximalDepth; i--)
                    node = node.parent();

                nodesAtCurrentDepth.add(node);
            }

            while (nodesAtCurrentDepth.size() > 1) {
                final var parents = nodesAtCurrentDepth.stream().map(Tree::parent).toList();
                nodesAtCurrentDepth = new TreeSet<>(parents);
            }

            return nodesAtCurrentDepth.stream().findFirst().get();
        }

    }

    public interface TreeBuilder<P, T extends Tree<T>> {

        T createRoot(P payload);

        T createChild(T parent, P payload);

    }

    public static <T extends Tree<T>, P extends Comparable<P>, E extends Edge<P>> @Nullable T treeFromEdges(Collection<E> edges, TreeBuilder<P, T> builder) {
        final var map = new TreeMap<P, EditableTree<P>>();

        for (final var edge : edges) {
            final var from = map.computeIfAbsent(edge.from(), EditableTree::new);
            final var to = map.computeIfAbsent(edge.to(), EditableTree::new);

            from.children.add(to);
            to.parent = from;
        }

        final var roots = map.values().stream().filter(node -> node.parent == null).toList();
        if (roots.size() != 1)
            return null;

        final var editableRoot = roots.get(0);
        final var root = builder.createRoot(editableRoot.payload);
        addChildren(editableRoot, root, builder);

        return root;
    }

    private static <T extends Tree<T>, P extends Comparable<P>, E extends Edge<P>> void addChildren(EditableTree<P> parent, T parentTree, TreeBuilder<P, T> builder) {
        for (final var child : parent.children) {
            final var childTree = builder.createChild(parentTree, child.payload);
            addChildren(child, childTree, builder);
        }
    }

    private static class EditableTree<P extends Comparable<P>> implements Tree<EditableTree<P>> {

        public @Nullable EditableTree<P> parent;

        public List<EditableTree<P>> children = new ArrayList<>();

        private final P payload;

        EditableTree(P payload) {
            this.payload = payload;
        }

        @Override public int compareTo(EditableTree<P> other) {
            return payload.compareTo(other.payload);
        }

        @Override public @Nullable EditableTree<P> parent() {
            return parent;
        }

        @Override public Collection<EditableTree<P>> children() {
            return children();
        }

    }

    /** Call callback recursively on each children of the tree. */
    public static <T extends TopDownTree<T>> void forEachDFS(T tree, Consumer<T> callback) {
        assert tree != null;

        Deque<T> stack = new ArrayDeque<>();
        stack.push(tree);

        while (!stack.isEmpty()) {
            final var current = stack.pop();
            callback.accept(current);
            current.children().forEach(stack::push);
        }
    }

    /** Doesn't need to be a tree. However, each node needs to explicitly return all children that should be processed next. */
    public static <T> void forEachDFS(T tree, Function<T, Collection<T>> callback) {
        assert tree != null;

        Deque<T> stack = new ArrayDeque<>();
        stack.push(tree);

        while (!stack.isEmpty()) {
            final var current = stack.pop();
            final var children = callback.apply(current);
            children.forEach(stack::push);
        }
    }

}
