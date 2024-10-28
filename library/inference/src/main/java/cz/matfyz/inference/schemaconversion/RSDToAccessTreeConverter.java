package cz.matfyz.inference.schemaconversion;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.UniqueNumberGenerator;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;
import cz.matfyz.core.rsd.Char;

/**
 * The {@code RSDToAccessTreeConverter} class is responsible for converting a {@link RecordSchemaDescription}
 * into an access tree structure represented by {@link AccessTreeNode}. It uses unique number generators
 * for generating keys and signatures required in the conversion process.
 */
public class RSDToAccessTreeConverter {

    private AccessTreeNode root;
    private final String kindName;
    private final UniqueNumberGenerator keyGenerator;
    private final UniqueNumberGenerator signatureGenerator;

    public RSDToAccessTreeConverter(String kindName, UniqueNumberGenerator keyGenerator, UniqueNumberGenerator signatureGenerator) {
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
        root = new AccessTreeNode(kindName, null, new Key(keyGenerator.next()), null, null, null, false);
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
                AccessTreeNode.Type state = rsdChild.getChildren().isEmpty() ? AccessTreeNode.Type.SIMPLE : AccessTreeNode.Type.COMPLEX;
                BaseSignature signature = Signature.createBase(signatureGenerator.next());
                Key keyChild = new Key(keyGenerator.next());
                Min min = findMin(rsdParent, rsdChild);

                if (rsdChild.getName().equals("_")) { // Check for mongo identifier
                    buildAccessTree(rsdChild, keyParent, i++, currentNode);
                } else if (!rsdChild.getName().equals("_id")) {
                    String label = "";
                    AccessTreeNode child = new AccessTreeNode(rsdChild.getName(), signature, keyChild, keyParent, label, min, isArray);
                    currentNode.addChild(child);

                    // Add _index node if parent is array and the _index is not yet present
                    if (isArray && !hasIndexChild(child)) {
                        BaseSignature signatureIndex = Signature.createBase(signatureGenerator.next());
                        Key keyIndex = new Key(keyGenerator.next());
                        AccessTreeNode indexChild = new AccessTreeNode("_index", signatureIndex, keyIndex, keyChild, "", Min.ONE, false);
                        child.addChild(indexChild);
                    }
                    buildAccessTree(rsdChild, keyChild, i++, child);
                }
            }
        }
    }

    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }

    private boolean hasIndexChild(AccessTreeNode node) {
        for (AccessTreeNode childNode : node.getChildren()) {
            if (childNode.name.equals("_index")) {
                return true;
            }
        }
        return false;
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

    private String createLabel(RecordSchemaDescription rsd, boolean isArray) {
        if (isArray)
            return SchemaConverter.Label.RELATIONAL.name();

        if (rsd.getUnique() == Char.TRUE)
            return SchemaConverter.Label.IDENTIFIER.name();

        return null;
    }
}
