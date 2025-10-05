package cz.matfyz.inference.schemaconversion;

import java.util.Set;

import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObjex;
import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.SchemaWithMetadata;

/**
 * The {@code AccessTreeToSchemaCategoryConverter} class is responsible for converting an access tree structure
 * (represented by {@link AccessTreeNode}) into a schema category and associated metadata.
 * This class builds the schema and metadata objexes based on the provided access tree.
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
        final var objex = createSchemaObjex(currentNode, isRoot);

        if (!isRoot)
            createSchemaMorphism(currentNode, objex);

        for (AccessTreeNode childNode : currentNode.getChildren())
            buildSchemaCategory(childNode);
    }

    private SchemaObjex createSchemaObjex(AccessTreeNode node, boolean isRoot) {
        final var ids = isRoot || !node.getChildren().isEmpty()
            ? ObjexIds.createGenerated()
            : ObjexIds.createValue();
        final var label = isRoot ? kindName : node.name;

        final var objex = schema.addObjex(new SerializedObjex(node.key, ids));
        metadata.setObjex(objex, new MetadataObjex(label, Position.createDefault()));

        return objex;
    }

    private void createSchemaMorphism(AccessTreeNode node, SchemaObjex objex) {
        final var parentObjex = schema.getObjex(node.getParentKey());
        if (parentObjex == null)
            throw new RuntimeException("Error while creating morphism. Domain is null and codomain is " + objex.key());

        final SchemaObjex dom = node.isArrayType ? objex : parentObjex;
        final SchemaObjex cod = node.isArrayType ? parentObjex : objex;

        final var morphism = schema.addMorphism(new SerializedMorphism(node.signature, dom.key(), cod.key(), node.min, Set.of()));
        metadata.setMorphism(morphism, new MetadataMorphism(node.label));
    }
}
