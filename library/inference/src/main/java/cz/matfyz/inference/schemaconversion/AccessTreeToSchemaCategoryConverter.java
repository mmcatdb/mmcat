package cz.matfyz.inference.schemaconversion;

import java.util.Set;

import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObject;
import cz.matfyz.core.metadata.MetadataObject.Position;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.SchemaWithMetadata;

/**
 * The {@code AccessTreeToSchemaCategoryConverter} class is responsible for converting an access tree structure
 * (represented by {@link AccessTreeNode}) into a schema category and associated metadata.
 * This class builds the schema and metadata objects based on the provided access tree.
 */
public class AccessTreeToSchemaCategoryConverter {

    private final SchemaCategory schema;
    private final MetadataCategory metadata;
    private final String kindName;

    public AccessTreeToSchemaCategoryConverter(String kindName) {
        this.schema = new SchemaCategory();
        this.metadata = MetadataCategory.createEmpty(schema);
        this.kindName = kindName;
    }

    /**
     * Converts the given access tree into a schema category and associated metadata.
     */
    public SchemaWithMetadata convert(AccessTreeNode root) {
        buildSchemaCategory(root);
        return new SchemaWithMetadata(schema, metadata);
    }

    /**
     * Recursively builds the schema category from the provided access tree node.
     */
    private void buildSchemaCategory(AccessTreeNode currentNode) {
        final var isRoot = currentNode.getType() == AccessTreeNode.Type.ROOT;
        final var object = createSchemaObject(currentNode, isRoot);

        if (!isRoot)
            createSchemaMorphism(currentNode, object);

        for (AccessTreeNode childNode : currentNode.getChildren())
            buildSchemaCategory(childNode);
    }

    /**
     * Creates a schema object from the provided access tree node and adds it to the schema.
     */
    private SchemaObject createSchemaObject(AccessTreeNode node, boolean isRoot) {
        final var ids = isRoot || !node.getChildren().isEmpty()
            ? ObjectIds.createGenerated()
            : ObjectIds.createValue();

        final SchemaObject object = new SchemaObject(node.key, ids, SignatureId.createEmpty());
        schema.addObject(object);

        final var label = isRoot ? kindName : node.name;
        metadata.setObject(object, new MetadataObject(label, Position.createDefault()));

        return object;
    }

    private void createSchemaMorphism(AccessTreeNode node, SchemaObject schemaObject) {
        final var parentObject = schema.getObject(node.getParentKey());

        if (parentObject == null)
            throw new RuntimeException("Error while creating morphism. Domain is null and codomain is " + schemaObject.key());

        SchemaObject dom = parentObject;
        SchemaObject cod = schemaObject;

        if (node.isArrayType) {
            dom = schemaObject;
            cod = parentObject;
        }

        final SchemaMorphism sm = new SchemaMorphism(node.signature, dom, cod, node.min, Set.of());
        schema.addMorphism(sm);
        metadata.setMorphism(sm, new MetadataMorphism(node.label));
    }
}
