package cz.matfyz.inference.schemaconversion;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObjex;
import cz.matfyz.core.metadata.MetadataObjex.Position;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

import java.awt.Dimension;
import java.util.*;

/**
 * The {@code Layout} class provides functionality for applying various graph layout algorithms from the JUNG library to a schema category.
 * This includes methods for calculating positions of schema objexes within a graph based on the chosen layout type.
 */
public class Layout {

    // Constants for layout configuration
    private static final int MIN_LAYOUT_SIZE = 600;
    private static final double SIZE_MULTIPLIER = 200.0;
    private static final int INITIAL_STEPS = 1000;
    private static final int MIN_SUBGRAPH_SIZE = 100;
    private static final int SUBGRAPH_PADDING = 50;

    private Layout() {}

    /**
     * Updates positions of objexes in the given schema category.
     */
    public static void updatePositions(SchemaCategory schema, MetadataCategory metadata, Map<Key, Position> positionsMap) {
        for (final var entry : positionsMap.entrySet()) {
            final var key = entry.getKey();
            final var position = entry.getValue();

            final var objex = schema.getObjex(key);
            final var metadataObjex = metadata.getObjex(key);
            final var updatedMetadata = new MetadataObjex(metadataObjex.label, position);
            metadata.setObjex(objex, updatedMetadata);
        }
    }

    /**
     * Applies a layout to the metadata of the given schema category based on the specified layout type.
     */
    public static void applyToMetadata(SchemaCategory schema, MetadataCategory metadata, LayoutType layoutType) {
        final var positions = computeObjexesLayout(schema, layoutType);

        for (final SchemaObjex objex : schema.allObjexes()) {
            final var position = positions.get(objex.key());
            final var mo = metadata.getObjex(objex);
            metadata.setObjex(objex, new MetadataObjex(mo.label, position));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Key, Position> computeObjexesLayout(SchemaCategory schema, LayoutType layoutType) {
        final var subgraphs = partitionIntoSubgraphs(schema);
        final var positions = new HashMap<Key, Position>();

        final int canvasSize = calculateCanvasSize(schema.allObjexes().size());
        final int subgraphSize = calculateSubgraphSize(canvasSize, subgraphs.size());
        final int subgraphSpacing = subgraphSize + SUBGRAPH_PADDING;
        final int subgraphCountPerRow = calculateSubgraphCountPerRow(subgraphs.size());

        int currentXOffset = subgraphSpacing / 2;
        int currentYOffset = subgraphSpacing / 2;

        for (int i = 0; i < subgraphs.size(); i++) {
            DirectedSparseGraph<SchemaObjex, SchemaMorphism> subgraph = subgraphs.get(i);
            AbstractLayout<SchemaObjex, SchemaMorphism> layout = createLayout(subgraph, layoutType);

            layout.setSize(new Dimension(subgraphSize, subgraphSize));
            layout.initialize();

            if (layout instanceof FRLayout frLayout)
                runInitialLayoutSteps(frLayout);

            storeSubgraphPositions(subgraph, layout, positions, currentXOffset, currentYOffset);

            currentXOffset += subgraphSpacing;
            if ((i + 1) % subgraphCountPerRow == 0) {
                currentXOffset = subgraphSpacing / 2;
                currentYOffset += subgraphSpacing;
            }
        }

        return positions;
    }

    private static int calculateCanvasSize(int nodesAmount) {
        return Math.max(MIN_LAYOUT_SIZE, (int) (Math.log(nodesAmount + 1.0) * SIZE_MULTIPLIER));
    }

    private static int calculateSubgraphSize(int canvasSize, int subgraphCount) {
        return Math.max(MIN_SUBGRAPH_SIZE, canvasSize / (int) Math.sqrt(subgraphCount) - SUBGRAPH_PADDING);
    }

    private static int calculateSubgraphCountPerRow(int subgraphCount) {
        int subgraphCountPerRow = (int) Math.ceil(Math.sqrt(subgraphCount));
        while ((subgraphCount / subgraphCountPerRow) > (subgraphCountPerRow - 1)) {
            subgraphCountPerRow++;
        }
        return subgraphCountPerRow;
    }

    private static void runInitialLayoutSteps(FRLayout<SchemaObjex, SchemaMorphism> layout) {
        for (int j = 0; j < INITIAL_STEPS; j++) {
            layout.step();
        }
    }

    private static void storeSubgraphPositions(
        DirectedSparseGraph<SchemaObjex, SchemaMorphism> subgraph,
        AbstractLayout<SchemaObjex, SchemaMorphism> layout,
        Map<Key, Position> positions,
        int xOffset,
        int yOffset
    ) {
        for (final SchemaObjex node : subgraph.getVertices()) {
            final double x = layout.getX(node) + xOffset;
            final double y = layout.getY(node) + yOffset;
            positions.put(node.key(), new Position(x, y));
        }
    }

    private static List<DirectedSparseGraph<SchemaObjex, SchemaMorphism>> partitionIntoSubgraphs(SchemaCategory schema) {
        final var subgraphs = new ArrayList<DirectedSparseGraph<SchemaObjex, SchemaMorphism>>();
        final Set<SchemaObjex> visited = new HashSet<>();

        for (final SchemaObjex objex : schema.allObjexes()) {
            if (visited.contains(objex))
                continue;

            final var subgraph = new DirectedSparseGraph<SchemaObjex, SchemaMorphism>();
            final Queue<SchemaObjex> queue = new ArrayDeque<>();
            queue.add(objex);

            while (!queue.isEmpty()) {
                final SchemaObjex current = queue.poll();
                if (visited.contains(current))
                    continue;

                visited.add(current);
                subgraph.addVertex(current);

                for (final var morphism : current.from()) {
                    subgraph.addEdge(morphism, morphism.dom(), morphism.cod());
                    queue.add(morphism.cod());
                }

                for (final var morphism : current.to()) {
                    subgraph.addEdge(morphism, morphism.dom(), morphism.cod());
                    queue.add(morphism.dom());
                }
            }

            subgraphs.add(subgraph);
        }

        return subgraphs;
    }

    private static AbstractLayout<SchemaObjex, SchemaMorphism> createLayout(DirectedSparseGraph<SchemaObjex, SchemaMorphism> graph, LayoutType layoutType) {
        switch (layoutType) {
            case CIRCLE:
                return new CircleLayout<>(graph);
            case KK:
                return new KKLayout<>(graph);
            case ISOM:
                return new ISOMLayout<>(graph);
            case FR:
            default:
                return new FRLayout<>(graph);
        }
    }
}
