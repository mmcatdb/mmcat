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
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author jachymb.bartik
 */
public abstract class GraphUtils {

    public interface Edge<N extends Comparable<N>> {
        N from();
        N to();
    }

    public static record Component<N extends Comparable<N>, E extends Edge<N>>(
        int id,
        Set<N> nodes,
        List<E> edges
    ) implements Comparable<Component<N, E>> {
        @Override
        public int compareTo(Component<N, E> other) {
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

    public interface Tree<T extends Tree<T>> extends Comparable<T> {

        @Nullable T parent();

        Collection<T> children();

    }

    public static record TreePath<T extends Tree<T>>(
        // Source is the first element, root is the last.
        List<T> sourceToRoot,
        // Root is the first element, target is the last.
        List<T> rootToTarget
    ) {}

    public static <T extends Tree<T>> TreePath<T> findPath(T source, T target) {
        return new TreePathFinder<T>().findPath(source, target);
    }

    private static class TreePathFinder<T extends Tree<T>> {

        public TreePath<T> findPath(T source, T target) {
            if (source.equals(target))
                return new TreePath<>(List.of(source), List.of(target));

            final var sourceToRoot = computePathToRoot(source);
            final var targetToRoot = computePathToRoot(target);

            while (
                sourceToRoot.size() >= 2 &&
                targetToRoot.size() >= 2 &&
                sourceToRoot.get(sourceToRoot.size() - 2).equals(targetToRoot.get(targetToRoot.size() - 2))
            ) {
                sourceToRoot.remove(sourceToRoot.size() - 1);
                targetToRoot.remove(targetToRoot.size() - 1);
            }

            final var rootToTarget = new ArrayList<T>();
            for (int i = targetToRoot.size() - 1; i >= 0; i--)
                rootToTarget.add(targetToRoot.get(i));

            return new TreePath<>(sourceToRoot, rootToTarget);
        }

        private List<T> computePathToRoot(T node) {
            final var output = new ArrayList<T>();
            T current = node;

            while (current != null) {
                output.add(current);
                current = current.parent();
            }

            return output;
        }

    }

    @Nullable
    public static <T extends Tree<T>> T findDFS(T tree, Predicate<T> predicate) {
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

    @Nullable
    public static <T extends Tree<T>> T findBFS(T tree, Predicate<T> predicate) {
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
        return new TreeSubrootFinder<T>(root).findSubroot(nodes);
    }

    private static class TreeSubrootFinder<T extends Tree<T>> {

        private Map<T, Integer> depths = new TreeMap<>();

        public TreeSubrootFinder(T root) {
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

    @Nullable
    public static <T extends Tree<T>, P extends Comparable<P>, E extends Edge<P>> T treeFromEdges(Collection<E> edges, TreeBuilder<P, T> builder) {
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

        @Nullable
        public EditableTree<P> parent;

        public List<EditableTree<P>> children = new ArrayList<>();

        private final P payload;

        public EditableTree(P payload) {
            this.payload = payload;
        }

        @Override
        public int compareTo(EditableTree<P> other) {
            return payload.compareTo(other.payload);
        }

        @Override
        @Nullable
        public EditableTree<P> parent() {
            return parent;
        }

        @Override
        public Collection<EditableTree<P>> children() {
            return children();
        }

    }

    public interface TreeBuilder<P, T extends Tree<T>> {

        T createRoot(P payload);

        T createChild(T parent, P payload);

    }

}
