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

    /**
     * Constructs a new {@code AccessTreeToSchemaCategoryConverter} with a specified kind name.
     *
     * @param kindName The kind name to be used as the label for the root node in the schema.
     */
    public AccessTreeToSchemaCategoryConverter(String kindName) {
        this.schema = new SchemaCategory();
        this.metadata = MetadataCategory.createEmpty(schema);
        this.kindName = kindName;
    }

    /**
     * Converts the given access tree into a schema category and associated metadata.
     *
     * @param root The root node of the access tree to be converted.
     * @return A {@link SchemaWithMetadata} object containing the converted schema and metadata.
     */
    public SchemaWithMetadata convert(AccessTreeNode root) {
        buildSchemaCategory(root);
        return new SchemaWithMetadata(schema, metadata);
    }

    /**
     * Recursively builds the schema category from the provided access tree node.
     *
     * @param currentNode The current node in the access tree being processed.
     */
    private void buildSchemaCategory(AccessTreeNode currentNode) {
        final var isRoot = currentNode.getState() == AccessTreeNode.State.ROOT;
        final var object = createSchemaObject(currentNode, isRoot);

        if (!isRoot)
            createSchemaMorphism(currentNode, object);

        for (AccessTreeNode childNode : currentNode.getChildren())
            buildSchemaCategory(childNode);
    }

    /**
     * Creates a schema object from the provided access tree node and adds it to the schema.
     *
     * @param node The access tree node representing the schema object.
     * @param isRoot {@code true} if the node is the root of the tree; {@code false} otherwise.
     * @return The created {@link SchemaObject}.
     */
    private SchemaObject createSchemaObject(AccessTreeNode node, boolean isRoot) {
        final var ids = isRoot || !node.getChildren().isEmpty()
            ? ObjectIds.createGenerated()
            : ObjectIds.createValue();

        final SchemaObject object = new SchemaObject(node.getKey(), ids, SignatureId.createEmpty());
        schema.addObject(object);

        final var label = isRoot ? kindName : node.getName();
        metadata.setObject(object, new MetadataObject(label, Position.createDefault()));

        return object;
    }

    /**
     * Creates a schema morphism between the parent and current schema objects based on the provided access tree node.
     *
     * @param node The access tree node representing the schema morphism.
     * @param schemaObject The current schema object.
     */
    private void createSchemaMorphism(AccessTreeNode node, SchemaObject schemaObject) {
        final var parentObject = schema.getObject(node.getParentKey());

        if (parentObject == null) {
            System.out.println("Node key: " + node.getKey());
            System.out.println("Parent key: " + node.getParentKey());

            throw new RuntimeException("Error while creating morphism. Domain is null and codomain is " + schemaObject.key());
        }

        SchemaObject dom = parentObject;
        SchemaObject cod = schemaObject;

        if (node.getIsArrayType()) {
            dom = schemaObject;
            cod = parentObject;
        }

        final SchemaMorphism sm = new SchemaMorphism(node.getSignature(), dom, cod, node.getMin(), Set.of());
        schema.addMorphism(sm);

        metadata.setMorphism(sm, new MetadataMorphism(node.getLabel()));
    }
}
