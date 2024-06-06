package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.*;
import cz.matfyz.core.schema.SchemaCategory;

public class MappingCreator {
    public Key rootKey;
    public AccessTreeNode root;
    private MappingBuilder builder = new MappingBuilder();

    public MappingCreator(Key rootKey, AccessTreeNode root) {
        this.rootKey = rootKey;
        this.root = root;
    }

    public Mapping createMapping(SchemaCategory sc, String kindName) {
        ComplexProperty accessPath = buildComplexPropertyFromNode(root);
        return Mapping.create(sc, rootKey, kindName, accessPath);
    }

    public ComplexProperty buildComplexPropertyFromNode(AccessTreeNode node) {
        List<AccessPath> subpaths = new ArrayList<>();

        for (AccessTreeNode child : node.getChildren()) {
            if (child.getState() == AccessTreeNode.State.Simple) {
                subpaths.add(builder.simple(child.getName(), getDirectedSignature(child)));
            } else {
                subpaths.add(buildComplexPropertyFromNode(child));
            }
        }

        if (node.getState() == AccessTreeNode.State.Root) {
            System.out.println("Adding root to mapping");
            return builder.root(subpaths.toArray(new AccessPath[0]));
        } else {
            System.out.println("Creating complex property for: " + node.getName());
            return builder.complex(node.getName(), getDirectedSignature(node), subpaths.toArray(new AccessPath[0]));
        }
    }

    private static BaseSignature getDirectedSignature(AccessTreeNode node) {
        return node.isArrayType ? node.getSig().dual() : node.getSig();
    }

}
