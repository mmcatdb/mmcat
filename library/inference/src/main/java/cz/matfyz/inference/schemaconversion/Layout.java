package cz.matfyz.inference.schemaconversion;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObject;
import cz.matfyz.core.metadata.MetadataObject.Position;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

import java.awt.Dimension;
import java.util.*;

/**
 * The {@code Layout} class provides functionality for applying various graph layout algorithms
 * from the JUNG library to a schema category. This includes methods for calculating positions
 * of schema objects within a graph based on the chosen layout type.
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
     * Updates objects positions in the given schema category.
     */
    public static void updatePositions(SchemaCategory schema, MetadataCategory metadata, Map<Key, Position> positionsMap) {
        for (Map.Entry<Key, Position> entry : positionsMap.entrySet()) {
            final var key = entry.getKey();
            final var position = entry.getValue();

            final var object = schema.getObject(key);
            final var metadataObject = metadata.getObject(key);
            final var updatedMetadata = new MetadataObject(metadataObject.label, position);
            metadata.setObject(object, updatedMetadata);
        }
    }

    /**
     * Applies a layout to the metadata of the given schema category based on the specified layout type.
     */
    public static void applyToMetadata(SchemaCategory schema, MetadataCategory metadata, LayoutType layoutType) {
        final var positions = computeObjectsLayout(schema, layoutType);

        for (final SchemaObject object : schema.allObjects()) {
            final var position = positions.get(object.key());
            final var mo = metadata.getObject(object);
            metadata.setObject(object, new MetadataObject(mo.label, position));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Key, Position> computeObjectsLayout(SchemaCategory schema, LayoutType layoutType) {
        List<DirectedSparseGraph<SchemaObject, SchemaMorphism>> subgraphs = partitionIntoSubgraphs(schema);
        final var positions = new HashMap<Key, Position>();

        int canvasSize = calculateCanvasSize(schema.allObjects().size());
        int subgraphSize = calculateSubgraphSize(canvasSize, subgraphs.size());
        int subgraphSpacing = subgraphSize + SUBGRAPH_PADDING;
        int subgraphCountPerRow = calculateSubgraphCountPerRow(subgraphs.size());

        int currentXOffset = subgraphSpacing / 2;
        int currentYOffset = subgraphSpacing / 2;

        for (int i = 0; i < subgraphs.size(); i++) {
            DirectedSparseGraph<SchemaObject, SchemaMorphism> subgraph = subgraphs.get(i);
            AbstractLayout<SchemaObject, SchemaMorphism> layout = createLayout(subgraph, layoutType);

            layout.setSize(new Dimension(subgraphSize, subgraphSize));
            layout.initialize();

            if (layout instanceof FRLayout frLayout) {
                runInitialLayoutSteps(frLayout);
            }

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

    private static void runInitialLayoutSteps(FRLayout<SchemaObject, SchemaMorphism> layout) {
        for (int j = 0; j < INITIAL_STEPS; j++) {
            layout.step();
        }
    }

    private static void storeSubgraphPositions(DirectedSparseGraph<SchemaObject, SchemaMorphism> subgraph,
                                               AbstractLayout<SchemaObject, SchemaMorphism> layout,
                                               Map<Key, Position> positions,
                                               int xOffset,
                                               int yOffset) {
        for (SchemaObject node : subgraph.getVertices()) {
            double x = layout.getX(node) + xOffset;
            double y = layout.getY(node) + yOffset;
            positions.put(node.key(), new Position(x, y));
        }
    }

    private static List<DirectedSparseGraph<SchemaObject, SchemaMorphism>> partitionIntoSubgraphs(SchemaCategory schema) {
        List<DirectedSparseGraph<SchemaObject, SchemaMorphism>> subgraphs = new ArrayList<>();
        Set<SchemaObject> visited = new HashSet<>();

        for (SchemaObject object : schema.allObjects()) {
            if (!visited.contains(object)) {
                DirectedSparseGraph<SchemaObject, SchemaMorphism> subgraph = new DirectedSparseGraph<>();
                Queue<SchemaObject> queue = new LinkedList<>();
                queue.add(object);

                while (!queue.isEmpty()) {
                    SchemaObject current = queue.poll();
                    if (!visited.contains(current)) {
                        visited.add(current);
                        subgraph.addVertex(current);

                        for (SchemaMorphism morphism : schema.allMorphisms()) {
                            if (morphism.dom().equals(current) || morphism.cod().equals(current)) {
                                subgraph.addEdge(morphism, morphism.dom(), morphism.cod());
                                SchemaObject next = morphism.dom().equals(current) ? morphism.cod() : morphism.dom();
                                if (!visited.contains(next)) {
                                    queue.add(next);
                                }
                            }
                        }
                    }
                }
                subgraphs.add(subgraph);
            }
        }
        return subgraphs;
    }

    private static AbstractLayout<SchemaObject, SchemaMorphism> createLayout(DirectedSparseGraph<SchemaObject, SchemaMorphism> graph, LayoutType layoutType) {
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
