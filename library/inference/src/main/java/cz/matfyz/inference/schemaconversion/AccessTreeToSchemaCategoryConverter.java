package cz.matfyz.inference.schemaconversion;

import java.util.HashSet;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.UniqueNumberGenerator;

public class AccessTreeToSchemaCategoryConverter {

    private final SchemaCategory schemaCategory;
    private final String kindName;

    public AccessTreeToSchemaCategoryConverter(String categoryLabel, String kindName) {
        this.schemaCategory = new SchemaCategory(categoryLabel);
        this.kindName = kindName;
    }

    public SchemaCategory convert(AccessTreeNode root) {
        buildSchemaCategory(root);
        return schemaCategory;
    }

    private void buildSchemaCategory(AccessTreeNode currentNode) {
        SchemaObject currentObject;
        if (currentNode.getState() == AccessTreeNode.State.ROOT) {
            currentObject = new SchemaObject(currentNode.getKey(), kindName, ObjectIds.createGenerated(), SignatureId.createEmpty());
            schemaCategory.addObject(currentObject);
        } else {
            System.out.println("Creating SO and SM for node: " + currentNode.getName());
            currentObject = createSchemaObject(currentNode);
            createSchemaMorphism(currentNode, currentObject);
        }

        for (AccessTreeNode childNode : currentNode.getChildren()) {
            buildSchemaCategory(childNode);
        }
    }

    private SchemaObject createSchemaObject(AccessTreeNode node) {
        ObjectIds ids = node.getChildren().isEmpty() ? ObjectIds.createValue() : ObjectIds.createGenerated();
        SchemaObject object = new SchemaObject(node.getKey(), node.getName(), ids, SignatureId.createEmpty());
        schemaCategory.addObject(object);
        return object;
    }

    private void createSchemaMorphism(AccessTreeNode node, SchemaObject schemaObject) {
        SchemaObject schemaObjectParent = schemaCategory.getObject(node.getParentKey());

        if (schemaObjectParent == null) {
            System.out.println("SK after accessing the parent node");
            System.out.println(schemaCategory.allObjects());
            System.out.println("Error while creating morphism. Domain is null and codomain is " + schemaObject.label());
            System.out.println("Node key: " + node.getKey());
            System.out.println("Parent key: " + node.getParentKey());
        }

        SchemaObject dom = schemaObjectParent;
        SchemaObject cod = schemaObject;

        if (node.getIsArrayType()) {
            dom = schemaObject;
            cod = schemaObjectParent;
        }

        SchemaMorphism sm = new SchemaMorphism(node.getSignature(), node.getLabel(), node.getMin(), new HashSet<>(), dom, cod);
        schemaCategory.addMorphism(sm);
    }
}
