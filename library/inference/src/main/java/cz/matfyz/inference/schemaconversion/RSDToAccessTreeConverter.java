package cz.matfyz.inference.schemaconversion;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.identifiers.Key.KeyGenerator;
import cz.matfyz.core.identifiers.Signature.SignatureGenerator;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.DataType;

/**
 * The {@code RSDToAccessTreeConverter} class is responsible for converting a {@link RecordSchemaDescription}
 * into an access tree structure represented by {@link AccessTreeNode}. It uses unique number generators
 * for generating keys and signatures required in the conversion process.
 */
public class RSDToAccessTreeConverter {

    // FIXME Use special name.
    /** @deprecated Use special name. */
    public static final String INDEX_LABEL = "_index";
    // FIXME Use special name.
    /** @deprecated Use special name. */
    public static final String VALUE_LABEL = "_value";

    // FIXME This should by handled by the wrapper.
    /** @deprecated This should be handled by the wrapper. */
    public static final String MONGO_IDENTIFIER = "_id";

    private AccessTreeNode root;
    private final String kindName;
    private final KeyGenerator keyGenerator;
    private final SignatureGenerator signatureGenerator;

    public RSDToAccessTreeConverter(String kindName, KeyGenerator keyGenerator, SignatureGenerator signatureGenerator) {
        this.keyGenerator = keyGenerator;
        this.signatureGenerator = signatureGenerator;
        this.kindName = kindName;
    }

    /**
     * Converts the given {@link RecordSchemaDescription} into an {@link AccessTreeNode} representing
     * the access tree structure.
     */
    public AccessTreeNode convert(RecordSchemaDescription rsd) {
        rsd.setName(TypedName.ROOT);
        root = new AccessTreeNode(kindName, null, keyGenerator.next(), null, null, null, false);
        convertChildren(rsd, root);

        root.transformArrayNodes();
        return root;
    }

    private void convertChildren(RecordSchemaDescription parentRsd, AccessTreeNode parentNode) {
        for (final var childRsd : parentRsd.getChildren()) {
            // FIXME This should by handled by the wrapper.
            if (childRsd.getName().equals(MONGO_IDENTIFIER))
                continue;

            // FIXME Use special name.
            final String name = (parentNode.isArrayType && childRsd.getName().equals(RecordSchemaDescription.ROOT_SYMBOL)) ? VALUE_LABEL : childRsd.getName();

            final var childNode = new AccessTreeNode(
                name,
                signatureGenerator.next(),
                keyGenerator.next(),
                parentNode.key,
                "",
                findMin(parentRsd, childRsd),
                isArrayType(childRsd)
            );
            parentNode.addChild(childNode);

            convertChildren(childRsd, childNode);
        }

        if (parentNode.isArrayType) {
            addChildIfMissing(parentNode, INDEX_LABEL);
            addChildIfMissing(parentNode, VALUE_LABEL);
        }
    }

    private boolean isArrayType(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & DataType.ARRAY) != 0;
    }

    private void addChildIfMissing(AccessTreeNode node, String label) {
        if (hasChildWithLabel(node, label))
            return;

        node.addChild(new AccessTreeNode(
            label,
            signatureGenerator.next(),
            keyGenerator.next(),
            node.key,
            "",
            Min.ONE,
            false
        ));
    }

    private boolean hasChildWithLabel(AccessTreeNode node, String label) {
        for (final AccessTreeNode childNode : node.getChildren()) {
            if (childNode.name.equals(label))
                return true;
        }
        return false;
    }

    private Min findMin(RecordSchemaDescription parent, RecordSchemaDescription child) {
        final var whoKnows1 = parent.getShareTotal() > child.getShareTotal() && parent.getShareFirst() > child.getShareFirst();
        final var whoKnows2 = parent.getShareTotal() < child.getShareTotal() && parent.getShareFirst() == child.getShareFirst();

        return whoKnows1 || whoKnows2 ? Min.ZERO : Min.ONE;
    }

}
