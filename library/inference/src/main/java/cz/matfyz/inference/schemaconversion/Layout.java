package cz.matfyz.inference.schemaconversion;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObject;
import cz.matfyz.core.metadata.MetadataObject.Position;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

public class Layout {

    // Constants for layout configuration.

    private static final int MIN_LAYOUT_SIZE = 600;
    private static final double SIZE_MULTIPLIER = 200.0;
    private static final int INITIAL_STEPS = 1000;

    private static final double ATTRACTION_MULTIPLIER_LARGE = 0.75;
    private static final double REPULSION_MULTIPLIER_LARGE = 2.0;

    private static final double ATTRACTION_MULTIPLIER_MEDIUM = 0.85;
    private static final double REPULSION_MULTIPLIER_MEDIUM = 1.5;

    private static final double ATTRACTION_MULTIPLIER_SMALL = 1.0;
    private static final double REPULSION_MULTIPLIER_SMALL = 1.0;

    private Layout() {}

    public static void applyToMetadata(SchemaCategory schema, MetadataCategory metadata) {
        final var positions = computeObjectsLayout(schema);

        for (final SchemaObject object : schema.allObjects()) {
            final var position = positions.get(object.key());
            final var mo = metadata.getObject(object);
            metadata.setObject(object, new MetadataObject(mo.label, position));
        }
    }

    /**
     * Layout algorithm using JUNG library.
     */
    private static Map<Key, Position> computeObjectsLayout(SchemaCategory schema) {
        final var graph = createGraphFromSchemaCategory(schema);

        // Determine the layout size based on the number of nodes
        final int nodesAmount = schema.allObjects().size();
        final int layoutSize = Math.max(MIN_LAYOUT_SIZE, (int) (Math.log(nodesAmount + 1.0) * SIZE_MULTIPLIER));

        final var layout = new FRLayout<>(graph);
        layout.setSize(new Dimension(layoutSize, layoutSize));

        // Adjust attraction and repulsion multipliers based on the graph size
        if (nodesAmount > 50) {
            layout.setAttractionMultiplier(ATTRACTION_MULTIPLIER_LARGE);
            layout.setRepulsionMultiplier(REPULSION_MULTIPLIER_LARGE);
        } else if (nodesAmount > 20) {
            layout.setAttractionMultiplier(ATTRACTION_MULTIPLIER_MEDIUM);
            layout.setRepulsionMultiplier(REPULSION_MULTIPLIER_MEDIUM);
        } else {
            layout.setAttractionMultiplier(ATTRACTION_MULTIPLIER_SMALL);
            layout.setRepulsionMultiplier(REPULSION_MULTIPLIER_SMALL);
        }

        for (int i = 0; i < INITIAL_STEPS; i++)
            layout.step();

        final var positions = new HashMap<Key, Position>();
        for (final SchemaObject node : graph.getVertices()) {
            final var position = new Position(layout.getX(node), layout.getY(node));
            positions.put(node.key(), position);
        }

        return positions;
    }

    private static DirectedSparseGraph<SchemaObject, SchemaMorphism> createGraphFromSchemaCategory(SchemaCategory schema) {
        final var graph = new DirectedSparseGraph<SchemaObject, SchemaMorphism>();

        for (SchemaObject object : schema.allObjects())
            graph.addVertex(object);

        for (SchemaMorphism morphism : schema.allMorphisms())
            graph.addEdge(morphism, morphism.dom(), morphism.cod());

        return graph;
    }

}
