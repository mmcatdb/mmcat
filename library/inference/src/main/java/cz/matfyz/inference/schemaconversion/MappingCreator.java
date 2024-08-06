package cz.matfyz.inference.schemaconversion;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.*;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;

public class MappingCreator {
    public final Key rootKey;
    public final AccessTreeNode root;
    private final MappingBuilder builder;

    public MappingCreator(Key rootKey, AccessTreeNode root) {
        this.rootKey = rootKey;
        this.root = root;
        this.builder = new MappingBuilder();
    }

    public Mapping createMapping(SchemaCategory schemaCategory, String kindName) {
        ComplexProperty accessPath = buildComplexPropertyFromNode(root);
        return Mapping.create(schemaCategory, rootKey, kindName, accessPath);
    }

    public ComplexProperty buildComplexPropertyFromNode(AccessTreeNode node) {
        List<AccessPath> subpaths = new ArrayList<>();

        for (AccessTreeNode child : node.getChildren()) {
            if (child.getState() == AccessTreeNode.State.SIMPLE) {
                subpaths.add(builder.simple(child.getName(), getDirectedSignature(child)));
            } else {
                subpaths.add(buildComplexPropertyFromNode(child));
            }
        }

        if (node.getState() == AccessTreeNode.State.ROOT) {
            System.out.println("Adding root to mapping");
            return builder.root(subpaths.toArray(new AccessPath[0]));
        } else {
            System.out.println("Creating complex property for: " + node.getName());
            return builder.complex(node.getName(), getDirectedSignature(node), subpaths.toArray(new AccessPath[0]));
        }
    }

    private static BaseSignature getDirectedSignature(AccessTreeNode node) {
        return node.getIsArrayType() ? node.getSignature().dual() : node.getSignature();
    }

}
