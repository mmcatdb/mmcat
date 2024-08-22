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

public class AccessTreeToSchemaCategoryConverter {

    private final SchemaCategory schema;
    private final MetadataCategory metadata;
    private final String kindName;

    public AccessTreeToSchemaCategoryConverter(String kindName) {
        this.schema = new SchemaCategory();
        this.metadata = MetadataCategory.createEmpty(schema);
        this.kindName = kindName;
    }

    public SchemaWithMetadata convert(AccessTreeNode root) {
        buildSchemaCategory(root);
        return new SchemaWithMetadata(schema, metadata);
    }

    private void buildSchemaCategory(AccessTreeNode currentNode) {
        final var isRoot = currentNode.getState() == AccessTreeNode.State.ROOT;
        final var object = createSchemaObject(currentNode, isRoot);

        if (!isRoot)
            createSchemaMorphism(currentNode, object);

        for (AccessTreeNode childNode : currentNode.getChildren())
            buildSchemaCategory(childNode);
    }

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
