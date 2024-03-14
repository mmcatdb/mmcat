package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.Signature;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaGraph {

    private final Map<SchemaObject, Node> nodes = new TreeMap<>();
    private final Set<Edge> edges = new TreeSet<>();

    /**
     * Creates a graph from all given morphisms (objects are automatically defined by the morphisms).
     */
    public SchemaGraph(Collection<SchemaMorphism> morphisms) {
        morphisms.forEach(m -> {
            var dom = nodes.computeIfAbsent(m.dom(), Node::new);
            var cod = nodes.computeIfAbsent(m.cod(), Node::new);
            var edge = Edge.create(m, dom, cod);
            edges.add(edge);
        });
    }

    private SchemaGraph() {}

    private SchemaGraph copy() {
        final var copy = new SchemaGraph();
        nodes.keySet().forEach(object -> copy.nodes.put(object, new Node(object)));
        edges.forEach(edge -> {
            var domCopy = copy.nodes.get(edge.source.object);
            var codCopy = copy.nodes.get(edge.target.object);
            var edgeCopy = Edge.create(edge.morphism, domCopy, codCopy);
            copy.edges.add(edgeCopy);
        });

        return copy;
    }

    /**
     * If either object cannot be found or there is no path, null is returned.
     */
    @Nullable
    public List<Signature> findPath(SchemaObject sourceObject, SchemaObject targetObject) {
        final var source = nodes.get(sourceObject);
        final var target = nodes.get(targetObject);
        if (source == null || target == null)
            return null;

        return new FindPath(source, target).run();
    }

    /**
     * Root is a node to which lead zero edges.
     */
    public List<SchemaObject> findRoots() {
        return nodes.values().stream()
            .filter(node -> node.edges.stream().allMatch(edge -> edge.source == node))
            .map(node -> node.object).toList();
    }

    /**
     * Computes whether there are no cycles. Direction of morphisms is important - the graph is traversed only in the direction of the edges.
     */
    public boolean findIsDirectedTrees() {
        final var copy = copy();
        final var queue = new ArrayDeque<>(copy.nodes.values());
        final var deletedNodes = new TreeSet<Node>();

        while (!queue.isEmpty()) {
            final var node = queue.poll();
            // If the node is already deleted, we can skip it.
            if (deletedNodes.contains(node))
                continue;

            // We need to find only those nodes to which no edges lead so that we can eliminate them.
            if (!node.edges.stream().allMatch(edge -> edge.source == node))
                continue;

            deletedNodes.add(node);
            node.edges.forEach(edge -> {
                edge.target.edges.remove(edge);
                queue.addFirst(edge.target);
            });
        }

        return deletedNodes.size() == nodes.size();
    }

    /**
     * Computes whether there are no cycles. Direction of morphisms is not important - the graph is traversed both in and against the direction of the edges.
     */
    public boolean findIsTrees() {
        final var components = new ArrayList<Set<Node>>();
        final var visited = new TreeSet<Node>();
        nodes.values().forEach(node -> {
            if (!visited.contains(node)) {
                var component = findComponent(node);
                visited.addAll(component);
                components.add(component);
            }
        });

        return components.size() + edges.size() == nodes.size();
    }

    /**
     * Find the whole component that contains this node.
     */
    public Set<Node> findComponent(Node node) {
        final var output = new TreeSet<>(List.of(node));
        final var notVisited = new ArrayDeque<>(List.of(node));
        while (!notVisited.isEmpty()) {
            final var visitedNode = notVisited.poll();
            visitedNode.edges.forEach(edge -> {
                var otherNode = edge.otherNode(visitedNode);
                if (!output.contains(otherNode))
                    notVisited.addFirst(otherNode);
                output.add(otherNode);
            });
        }

        return output;
    }

    /**
     * Returns all morphisms that originates in the given object.
     */
    public List<SchemaMorphism> getChildren(SchemaObject object) {
        final var node = nodes.get(object);
        return node == null
            ? List.of()
            : node.edges.stream().filter(edge -> edge.source == node).map(edge -> edge.morphism).toList();
    }

    private static class Node implements Comparable<Node> {
        final SchemaObject object;
        final Set<Edge> edges = new TreeSet<>();

        Node(SchemaObject object) {
            this.object = object;
        }

        @Override public int compareTo(Node other) {
            return object.compareTo(other.object);
        }
    }

    private static class Edge implements Comparable<Edge> {
        final SchemaMorphism morphism;
        final Node source;
        final Node target;

        private Edge(SchemaMorphism morphism, Node source, Node target) {
            this.morphism = morphism;
            this.source = source;
            this.target = target;
        }

        static Edge create(SchemaMorphism morphism, Node source, Node target) {
            var edge = new Edge(morphism, source, target);

            source.edges.add(edge);
            target.edges.add(edge);

            return edge;
        }

        Node otherNode(Node node) {
            return node == source ? target : source;
        }

        @Override public int compareTo(Edge other) {
            return morphism.compareTo(other.morphism);
        }
    }

    private static class FindPath {
        private final Node source;
        private final Node target;

        FindPath(Node source, Node target) {
            this.source = source;
            this.target = target;
        }

        private record Path(Node lastNode, Edge lastEdge, Path rest) {}

        private Deque<Path> pathQueue;
        private Set<Node> visited;

        @Nullable List<Signature> run() {
            pathQueue = new ArrayDeque<>();
            visited = new TreeSet<>();
            visitNode(new Path(source, null, null));

            while (!pathQueue.isEmpty()) {
                var path = pathQueue.poll();
                if (path.lastNode == target)
                    return collectPath(path);

                visitNode(path);
            }

            return null;
        }

        private void visitNode(Path path) {
            var node = path.lastNode;
            if (visited.contains(node))
                return;

            visited.add(node);
            node.edges.forEach(e -> {
                var newPath = new Path(e.otherNode(node), e, path);
                pathQueue.add(newPath);
            });
        }

        private List<Signature> collectPath(Path path) {
            var output = new ArrayList<Signature>();
            while (path.lastEdge != null) {
                var signature = path.lastEdge.morphism.signature();
                var correctSignature = path.lastNode == path.lastEdge.target ? signature : signature.dual();
                output.add(correctSignature);
                path = path.rest;
            }
            Collections.reverse(output);

            return output;
        }

    }

}
