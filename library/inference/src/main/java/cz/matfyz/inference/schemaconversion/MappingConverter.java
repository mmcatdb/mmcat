package cz.matfyz.inference.schemaconversion;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
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
        ComplexProperty accessPath = buildComplexPropertyFromNode(root, null, null);
        return Mapping.create(datasource, kindName, schemaCategory, rootKey, accessPath);
    }

    /**
     * Builds a {@link ComplexProperty} from the given access tree node, recursively processing its children.
     */
    public ComplexProperty buildComplexPropertyFromNode(AccessTreeNode node, @Nullable String name, @Nullable Signature signature) {
        List<AccessPath> subpaths = new ArrayList<>();

        for (AccessTreeNode child : node.getChildren()) {
            // adjusted mapping for arrays - for now we dont support indexing
            if (child.isArrayType) {
                AccessTreeNode valueNode = getValueNodeForArray(child);
                Signature arraySignature = getArraySignature(child, valueNode);

                if (isComplexArray(valueNode))
                    subpaths.add(buildComplexPropertyFromNode(valueNode, child.name, arraySignature));
                else
                    subpaths.add(builder.simple(child.name, arraySignature));
            }
            else {
                if (child.getType() == AccessTreeNode.Type.SIMPLE)
                    subpaths.add(builder.simple(child.name, child.signature));
                else
                    subpaths.add(buildComplexPropertyFromNode(child, null, null));
            }
        }

        if (node.getType() == AccessTreeNode.Type.ROOT)
            return builder.root(subpaths.toArray(new AccessPath[0]));
        else if (name != null && signature != null)
            return builder.complex(name, signature, subpaths.toArray(new AccessPath[0]));
        else
            return builder.complex(node.name, node.signature, subpaths.toArray(new AccessPath[0]));
    }

    private Signature getArraySignature(AccessTreeNode node, AccessTreeNode valueNode) {
        Signature signature = Signature.concatenate(node.signature.dual(), valueNode.signature);
        return signature;
    }

    private AccessTreeNode getValueNodeForArray(AccessTreeNode node) {
        for (AccessTreeNode child: node.getChildren())
            if (child.name.equals(RSDToAccessTreeConverter.VALUE_LABEL))
                return child;

        throw new IllegalStateException("Value node for array node has not been found");
    }

    private boolean isComplexArray(AccessTreeNode valueNode) {
        return !valueNode.getChildren().isEmpty();
    }

}
