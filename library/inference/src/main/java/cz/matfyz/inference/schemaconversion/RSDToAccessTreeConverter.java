package cz.matfyz.inference.schemaconversion;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key.KeyGenerator;
import cz.matfyz.core.identifiers.Signature.SignatureGenerator;
import cz.matfyz.core.mapping.Name.TypedName;
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
        buildAccessTree(rsd, root.key, 1, root);
        root.transformArrayNodes();
        return root;
    }

    private void buildAccessTree(RecordSchemaDescription rsdParent, Key keyParent, int i, AccessTreeNode currentNode) {
        if (rsdParent.getChildren().isEmpty()) {
            return;
        }

        if (TypedName.ROOT.equals(rsdParent.getName())) {
            currentNode = root;
        }

        for (RecordSchemaDescription rsdChild : rsdParent.getChildren()) {
            if (rsdChild.getName().equals(MONGO_IDENTIFIER)) {
                continue;
            }

            boolean isArray = isTypeArray(rsdChild);
            String name = (isTypeArray(rsdParent) && rsdChild.getName().equals(RecordSchemaDescription.ROOT_SYMBOL)) ? VALUE_LABEL : rsdChild.getName();
            BaseSignature signature = signatureGenerator.next();
            Key keyChild = keyGenerator.next();
            String label = "";
            Min min = findMin(rsdParent, rsdChild);

            AccessTreeNode childNode = new AccessTreeNode(name, signature, keyChild, keyParent, label, min, isArray);
            currentNode.addChild(childNode);

            if (isArray)
                addChildIfMissing(childNode, keyChild, INDEX_LABEL);

            buildAccessTree(rsdChild, keyChild, i + 1, childNode);
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
