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
import java.util.HashMap;
import java.util.Map;

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

    private Layout() {}

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

    private static Map<Key, Position> computeObjectsLayout(SchemaCategory schema, LayoutType layoutType) {
        final var graph = createGraphFromSchemaCategory(schema);

        final int nodesAmount = schema.allObjects().size();
        final int layoutSize = Math.max(MIN_LAYOUT_SIZE, (int) (Math.log(nodesAmount + 1.0) * SIZE_MULTIPLIER));

        AbstractLayout<SchemaObject, SchemaMorphism> layout = createLayout(graph, layoutType);
        layout.setSize(new Dimension(layoutSize, layoutSize));

        // Perform initial steps for the FRLayout to improve layout quality
        if (layout instanceof FRLayout) {
            for (int i = 0; i < INITIAL_STEPS; i++) {
                ((FRLayout<SchemaObject, SchemaMorphism>) layout).step();
            }
        }

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
