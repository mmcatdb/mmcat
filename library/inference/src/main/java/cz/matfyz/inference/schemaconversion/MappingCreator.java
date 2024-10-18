package cz.matfyz.inference.schemaconversion;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.*;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;

/**
 * The {@code MappingCreator} class is responsible for creating mappings for a schema category
 * based on an access tree structure represented by {@link AccessTreeNode}. It utilizes a
 * {@link MappingBuilder} to construct complex properties and mappings.
 */
public class MappingCreator {

    /** The root key of the mapping. */
    public final Key rootKey;

    /** The root node of the access tree. */
    public final AccessTreeNode root;

    private final MappingBuilder builder;

    /**
     * Constructs a new {@code MappingCreator} with the specified root key and access tree node.
     *
     * @param rootKey The root key for the mapping.
     * @param root The root {@link AccessTreeNode} representing the access tree structure.
     */
    public MappingCreator(Key rootKey, AccessTreeNode root) {
        this.rootKey = rootKey;
        this.root = root;
        this.builder = new MappingBuilder();
    }

    /**
     * Creates a new {@link Mapping} for the given schema category and kind name.
     *
     * @param schemaCategory The {@link SchemaCategory} to which the mapping is applied.
     * @param kindName The kind name for the mapping.
     * @return A new {@link Mapping} instance.
     */
    public Mapping createMapping(SchemaCategory schemaCategory, String kindName) {
        ComplexProperty accessPath = buildComplexPropertyFromNode(root);
        return Mapping.create(schemaCategory, rootKey, kindName, accessPath);
    }

    /**
     * Builds a {@link ComplexProperty} from the given access tree node, recursively processing its children.
     *
     * @param node The {@link AccessTreeNode} from which to build the complex property.
     * @return A {@link ComplexProperty} representing the access tree structure starting from the given node.
     */
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
            return builder.root(subpaths.toArray(new AccessPath[0]));
        } else {
            return builder.complex(node.getName(), getDirectedSignature(node), subpaths.toArray(new AccessPath[0]));
        }
    }

    /**
     * Returns the directed signature of the given access tree node.
     * If the node represents an array type, the dual of its signature is returned; otherwise, the signature itself is returned.
     *
     * @param node The {@link AccessTreeNode} for which to get the directed signature.
     * @return The {@link BaseSignature} representing the directed signature of the node.
     */
    private static BaseSignature getDirectedSignature(AccessTreeNode node) {
        return node.getIsArrayType() ? node.getSignature().dual() : node.getSignature();
    }

}
