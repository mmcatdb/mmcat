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

    public AccessTreeNode convert(RecordSchemaDescription rsd) {
        rsd.setName("root");
        root = new AccessTreeNode(AccessTreeNode.State.ROOT, kindName, null, new Key(keyGenerator.next()), null, null, null, false);
        buildAccessTree(rsd, root.getKey(), 1, root);
        root.transformArrayNodes();
        return root;
    }

    private void buildAccessTree(RecordSchemaDescription rsdParent, Key keyParent, int i, AccessTreeNode currentNode) {
        if (!rsdParent.getChildren().isEmpty()) {
            if (rsdParent.getName().equals("root")) {
                currentNode = root;
            }
            if (currentNode != null & currentNode.getState() != AccessTreeNode.State.ROOT) {
                currentNode.setState(AccessTreeNode.State.COMPLEX);
            }

            for (RecordSchemaDescription rsdChild : rsdParent.getChildren()) {
               // if (!rsdChild.getName().equals("_")) { // excluding the "_" objects of arrays
                    boolean isArray = isTypeArray(rsdChild);
                    AccessTreeNode.State state = isArray ? AccessTreeNode.State.COMPLEX : AccessTreeNode.State.SIMPLE;
                    BaseSignature signature = Signature.createBase(signatureGenerator.next());
                    Key keyChild = new Key(keyGenerator.next());
                    Min min = findMin(rsdParent, rsdChild);

                    // String label = createLabel(rsdChild, isArray);
                    String label = null;

                    AccessTreeNode child = new AccessTreeNode(state, rsdChild.getName(), signature, keyChild, keyParent, label, min, isArray);
                    currentNode.addChild(child);

                    // add _index node, if parent is array and the _index is not yet present
                    if (isArray && !hasIndexChild(child)) {
                        BaseSignature signatureIndex = Signature.createBase(signatureGenerator.next());
                        Key keyIndex = new Key(keyGenerator.next());
                        AccessTreeNode indexChild = new AccessTreeNode(AccessTreeNode.State.SIMPLE, "_index", signatureIndex, keyIndex, keyChild, null, Min.ONE, false);
                        child.addChild(indexChild);
                    }

                    buildAccessTree(rsdChild, keyChild, i++, child);
             //   }
            }
        }
    }

    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }

    private boolean hasIndexChild(AccessTreeNode node) {
        for (AccessTreeNode childNode : node.getChildren()) {
            if (childNode.getName().equals("_index")) {
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
