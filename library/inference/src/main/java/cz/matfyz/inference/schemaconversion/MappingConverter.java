package cz.matfyz.inference.schemaconversion;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.*;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;

/**
 * The {@code MappingConverter} class is responsible for creating mappings for a schema category
 * based on an access tree structure represented by {@link AccessTreeNode}. It utilizes a
 * {@link AccessPathBuilder} to construct complex properties and mappings.
 */
public class MappingConverter {

    public final Key rootKey;
    public final AccessTreeNode root;

    private final AccessPathBuilder builder;

    public MappingConverter(Key rootKey, AccessTreeNode root) {
        this.rootKey = rootKey;
        this.root = root;
        this.builder = new AccessPathBuilder();
    }

    /**
     * Creates a new {@link Mapping} for the given schema category and kind name.
     */
    public Mapping createMapping(Datasource datasource, SchemaCategory schemaCategory, String kindName) {
        ComplexProperty accessPath = buildComplexPropertyFromNode(root);
        return Mapping.create(datasource, kindName, schemaCategory, rootKey, accessPath);
    }

    /**
     * Builds a {@link ComplexProperty} from the given access tree node, recursively processing its children.
     */
    public ComplexProperty buildComplexPropertyFromNode(AccessTreeNode node) {
        List<AccessPath> subpaths = new ArrayList<>();

        for (AccessTreeNode child : node.getChildren()) {
            if (child.getType() == AccessTreeNode.Type.SIMPLE)
                subpaths.add(builder.simple(child.name, getDirectedSignature(child)));
            else
                subpaths.add(buildComplexPropertyFromNode(child));
        }

        if (node.getType() == AccessTreeNode.Type.ROOT)
            return builder.root(subpaths.toArray(new AccessPath[0]));
        else
            return builder.complex(node.name, getDirectedSignature(node), subpaths.toArray(new AccessPath[0]));
    }

    /**
     * Returns the directed signature of the given access tree node.
     * If the node represents an array type, the dual of its signature is returned; otherwise, the signature itself is returned.
     */
    private static BaseSignature getDirectedSignature(AccessTreeNode node) {
        return node.isArrayType ? node.signature.dual() : node.signature;
    }

}
