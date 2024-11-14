package cz.matfyz.inference.schemaconversion;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key.KeyGenerator;
import cz.matfyz.core.identifiers.Signature.SignatureGenerator;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;

/**
 * The {@code RSDToAccessTreeConverter} class is responsible for converting a {@link RecordSchemaDescription}
 * into an access tree structure represented by {@link AccessTreeNode}. It uses unique number generators
 * for generating keys and signatures required in the conversion process.
 */
public class RSDToAccessTreeConverter {

    public static final String INDEX_LABEL = "_index";
    public static final String VALUE_LABEL = "_value";

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
        rsd.setName("root");
        root = new AccessTreeNode(kindName, null, keyGenerator.next(), null, null, null, false);
        buildAccessTree(rsd, root.key, 1, root);
        root.transformArrayNodes();
        return root;
    }

    private void buildAccessTree(RecordSchemaDescription rsdParent, Key keyParent, int i, AccessTreeNode currentNode) {
        if (!rsdParent.getChildren().isEmpty()) {
            if (rsdParent.getName().equals("root"))
                currentNode = root;

            for (RecordSchemaDescription rsdChild : rsdParent.getChildren()) {
                boolean isArray = isTypeArray(rsdChild);
                BaseSignature signature = signatureGenerator.next();
                Key keyChild = keyGenerator.next();
                Min min = findMin(rsdParent, rsdChild);

                if (rsdChild.getName().equals(RecordSchemaDescription.ROOT_SYMBOL)) { // Check for mongo identifier
                    buildAccessTree(rsdChild, keyParent, i++, currentNode);
                } else if (!rsdChild.getName().equals("_id")) {
                    String label = "";
                    AccessTreeNode child = new AccessTreeNode(rsdChild.getName(), signature, keyChild, keyParent, label, min, isArray);
                    currentNode.addChild(child);

                    // Add _index and _value nodes if parent is array and they are not yet present
                    if (isArray) {
                        addChildIfMissing(child, keyChild, INDEX_LABEL);
                        addChildIfMissing(child, keyChild, VALUE_LABEL);
                    }

                    buildAccessTree(rsdChild, keyChild, i++, child);
                }
            }
        }
    }

    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }

    private void addChildIfMissing(AccessTreeNode node, Key key, String label) {
        if (!hasChildWithLabel(node, label)) {
            addExtraNode(node, key, label);
        }
    }

    private boolean hasChildWithLabel(AccessTreeNode node, String label) {
        for (AccessTreeNode childNode : node.getChildren()) {
            if (childNode.name.equals(label)) {
                return true;
            }
        }
        return false;
    }

    private void addExtraNode(AccessTreeNode currentNode, Key currentKey, String label) {
        BaseSignature signatureIndex = signatureGenerator.next();
        Key keyIndex = keyGenerator.next();
        AccessTreeNode indexChild = new AccessTreeNode(label, signatureIndex, keyIndex, currentKey, "", Min.ONE, false);
        currentNode.addChild(indexChild);
    }

    private Min findMin(RecordSchemaDescription rsdParent, RecordSchemaDescription rsdChild) {
        int shareParentTotal = rsdParent.getShareTotal();
        int shareChildTotal = rsdChild.getShareTotal();

        int shareParentFirst = rsdParent.getShareFirst();
        int shareChildFirst = rsdChild.getShareFirst();

        if ((shareParentTotal > shareChildTotal && shareParentFirst > shareChildFirst) ||
            (shareParentTotal < shareChildTotal && shareParentFirst == shareChildFirst)) {
            return Min.ZERO;
        }
        return Min.ONE;
    }

}
