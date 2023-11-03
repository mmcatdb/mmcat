package cz.matfyz.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

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
    };

    public static <N extends Comparable<N>, E extends Edge<N>> Collection<Component<N, E>> findComponents(Collection<E> edges) {
        final var finder = new ComponentFinder<N, E>();
        edges.forEach(e -> finder.addEdge(e));

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

    public interface Tree<T extends Tree<T>> {

        @Nullable T parent();

        Collection<T> children();

    }

    public static record TreePath<T extends Tree<T>>(
        // Source is the first element, root is the last.
        List<T> sourceToRoot,
        // Root is the first element, target is the last.
        List<T> rootToTarget
    ) {};

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
    public static <T extends Tree<T>> T findDFS(T tree, Function<T, Boolean> predicate) {
        Stack<T> stack = new Stack<>();
        stack.push(tree);

        while (!stack.isEmpty()) {
            final var current = stack.pop();

            if (predicate.apply(current))
                return current;

            current.children().forEach(stack::push);
        }

        return null;
    }

    @Nullable
    public static <T extends Tree<T>> T findBFS(T tree, Function<T, Boolean> predicate) {
        Queue<T> queue = new LinkedList<>();
        queue.add(tree);

        while (!queue.isEmpty()) {
            final var current = queue.poll();

            if (predicate.apply(current))
                return current;

            current.children().forEach(queue::add);
        }

        return null;
    }

}
