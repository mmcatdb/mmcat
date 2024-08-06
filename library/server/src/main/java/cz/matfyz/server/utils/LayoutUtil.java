package cz.matfyz.server.utils;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

public class LayoutUtil {

    // Constants for layout configuration
    private static final int MIN_LAYOUT_SIZE = 600;
    private static final double SIZE_MULTIPLIER = 200.0;
    private static final int INITIAL_STEPS = 1000;

    private static final double ATTRACTION_MULTIPLIER_LARGE = 0.75;
    private static final double REPULSION_MULTIPLIER_LARGE = 2.0;

    private static final double ATTRACTION_MULTIPLIER_MEDIUM = 0.85;
    private static final double REPULSION_MULTIPLIER_MEDIUM = 1.5;

    private static final double ATTRACTION_MULTIPLIER_SMALL = 1.0;
    private static final double REPULSION_MULTIPLIER_SMALL = 1.0;

    private LayoutUtil() {
        throw new UnsupportedOperationException("Utility class LayoutUtil.");
    }

    /**
     * Layout algorithm using JUNG library.
     */
    public static Map<Key, Position> layoutObjects(SchemaCategory schemaCategory) {
        DirectedSparseGraph<SchemaObject, SchemaMorphism> graph = createGraphFromSchemaCategory(schemaCategory);

        // Determine the layout size based on the number of nodes
        int numNodes = schemaCategory.allObjects().size();
        int layoutSize = Math.max(MIN_LAYOUT_SIZE, (int) (Math.log(numNodes + 1.0) * SIZE_MULTIPLIER));

        FRLayout<SchemaObject, SchemaMorphism> layout = new FRLayout<>(graph);
        layout.setSize(new Dimension(layoutSize, layoutSize));

        // Adjust attraction and repulsion multipliers based on the graph size
        if (numNodes > 50) {
            layout.setAttractionMultiplier(ATTRACTION_MULTIPLIER_LARGE);
            layout.setRepulsionMultiplier(REPULSION_MULTIPLIER_LARGE);
        } else if (numNodes > 20) {
            layout.setAttractionMultiplier(ATTRACTION_MULTIPLIER_MEDIUM);
            layout.setRepulsionMultiplier(REPULSION_MULTIPLIER_MEDIUM);
        } else {
            layout.setAttractionMultiplier(ATTRACTION_MULTIPLIER_SMALL);
            layout.setRepulsionMultiplier(REPULSION_MULTIPLIER_SMALL);
        }

        for (int i = 0; i < INITIAL_STEPS; i++) { // Initialize positions
            layout.step();
        }

        Map<Key, Position> positions = new HashMap<>();
        for (SchemaObject node : graph.getVertices()) {
            double x = layout.getX(node);
            double y = layout.getY(node);
            positions.put(node.key(), new Position(x, y));
        }

        return positions;
    }

    private static DirectedSparseGraph<SchemaObject, SchemaMorphism> createGraphFromSchemaCategory(SchemaCategory schemaCategory) {
        DirectedSparseGraph<SchemaObject, SchemaMorphism> graph = new DirectedSparseGraph<>();
        for (SchemaObject o : schemaCategory.allObjects()) {
            graph.addVertex(o);
        }

        for (SchemaMorphism m : schemaCategory.allMorphisms()) {
            if (m.dom() != null && m.cod() != null)
                graph.addEdge(m, m.dom(), m.cod());
        }

        return graph;
    }
}
