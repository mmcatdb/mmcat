package cz.matfyz.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author jachymb.bartik
 */
public class GraphUtils {

    public interface Edge<N extends Comparable<N>> {
        N from();
        N to();
    }

    private GraphUtils() {}

    public record Component<N extends Comparable<N>, E extends Edge<N>>(
        Set<N> nodes,
        List<E> edges
    ) {};

    public static <N extends Comparable<N>, E extends Edge<N>> Collection<Component<N, E>> findComponents(Collection<E> edges) {
        final var finder = new ComponentsFinder<N, E>();
        edges.forEach(e -> finder.addEdge(e));

        return finder.getComponents();
    }

    private static class ComponentsFinder<N extends Comparable<N>, E extends Edge<N>> {

        private Map<N, Component<N, E>> components = new TreeMap<>();

        public void addEdge(E edge) {
            final N a = edge.from();
            final N b = edge.to();

            final var aComponent = components.get(a);
            var bComponent = components.get(b);
            
            if (aComponent == null) {
                if (bComponent == null) {
                    bComponent = new Component<>(new TreeSet<>(), new ArrayList<>());
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

        public Collection<Component<N, E>> getComponents() {
            return components.values();
        }

    }

}
