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

public class Layout {

    // Constants for layout configuration
    private static final int MIN_LAYOUT_SIZE = 600;
    private static final double SIZE_MULTIPLIER = 200.0;
    private static final int INITIAL_STEPS = 1000;

    private Layout() {}

    public static void applyToMetadata(SchemaCategory schema, MetadataCategory metadata, LayoutType layoutType) {
        final var positions = computeObjectsLayout(schema, layoutType);

        for (final SchemaObject object : schema.allObjects()) {
            final var position = positions.get(object.key());
            final var mo = metadata.getObject(object);
            metadata.setObject(object, new MetadataObject(mo.label, position));
        }
    }

    /**
     * Layout algorithm using JUNG library.
     */
    private static Map<Key, Position> computeObjectsLayout(SchemaCategory schema, LayoutType layoutType) {
        final var graph = createGraphFromSchemaCategory(schema);

        final int nodesAmount = schema.allObjects().size();
        final int layoutSize = Math.max(MIN_LAYOUT_SIZE, (int) (Math.log(nodesAmount + 1.0) * SIZE_MULTIPLIER));

        AbstractLayout<SchemaObject, SchemaMorphism> layout;

        switch (layoutType) {
            case CIRCLE:
                layout = new CircleLayout<>(graph);
                break;
            case KK:
                layout = new KKLayout<>(graph);
                break;
            case ISOM:
                layout = new ISOMLayout<>(graph);
                break;
            case FR:
            default:
                layout = new FRLayout<>(graph);
                break;
        }

        layout.setSize(new Dimension(layoutSize, layoutSize));

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

}
