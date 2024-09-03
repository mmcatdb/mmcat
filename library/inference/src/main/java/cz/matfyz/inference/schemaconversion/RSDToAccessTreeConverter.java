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

    /**
     * Constructs a new {@code RSDToAccessTreeConverter} with the specified kind name and unique number generators.
     *
     * @param kindName The kind name to be used as the label for the root node in the access tree.
     * @param keyGenerator The {@link UniqueNumberGenerator} for generating unique keys.
     * @param signatureGenerator The {@link UniqueNumberGenerator} for generating unique signatures.
     */
    public RSDToAccessTreeConverter(String kindName, UniqueNumberGenerator keyGenerator, UniqueNumberGenerator signatureGenerator) {
        this.keyGenerator = keyGenerator;
        this.signatureGenerator = signatureGenerator;
        this.kindName = kindName;
    }

    /**
     * Converts the given {@link RecordSchemaDescription} into an {@link AccessTreeNode} representing
     * the access tree structure.
     *
     * @param rsd The {@link RecordSchemaDescription} to be converted.
     * @return The root {@link AccessTreeNode} of the resulting access tree.
     */
    public AccessTreeNode convert(RecordSchemaDescription rsd) {
        rsd.setName("root");
        root = new AccessTreeNode(AccessTreeNode.State.ROOT, kindName, null, new Key(keyGenerator.next()), null, null, null, false);
        buildAccessTree(rsd, root.getKey(), 1, root);
        root.transformArrayNodes();
        return root;
    }

    /**
     * Recursively builds the access tree from the given parent schema description and adds nodes to the current node.
     *
     * @param rsdParent The parent {@link RecordSchemaDescription}.
     * @param keyParent The key of the parent node.
     * @param i The current index used for processing.
     * @param currentNode The current {@link AccessTreeNode} being processed.
     */
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

                    // Add _index node if parent is array and the _index is not yet present
                    if (isArray && !hasIndexChild(child)) {
                        BaseSignature signatureIndex = Signature.createBase(signatureGenerator.next());
                        Key keyIndex = new Key(keyGenerator.next());
                        AccessTreeNode indexChild = new AccessTreeNode(AccessTreeNode.State.SIMPLE, "_index", signatureIndex, keyIndex, keyChild, null, Min.ONE, false);
                        child.addChild(indexChild);
                    }

                    buildAccessTree(rsdChild, keyChild, i++, child);
                //}
            }
        }
    }

    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }

    /**
     * Checks if the given node has a child node with the name "_index".
     *
     * @param node The {@link AccessTreeNode} to check.
     * @return {@code true} if the node has a child named "_index"; {@code false} otherwise.
     */
    private boolean hasIndexChild(AccessTreeNode node) {
        for (AccessTreeNode childNode : node.getChildren()) {
            if (childNode.getName().equals("_index")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the minimum cardinality (Min) for a child schema description based on its parent's share values.
     *
     * @param rsdParent The parent {@link RecordSchemaDescription}.
     * @param rsdChild The child {@link RecordSchemaDescription}.
     * @return The {@link Min} cardinality for the child schema description.
     */
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

    /**
     * Creates a label for the schema description based on its type and uniqueness.
     *
     * @param rsd The {@link RecordSchemaDescription} for which to create the label.
     * @param isArray {@code true} if the schema represents an array; {@code false} otherwise.
     * @return The label for the schema description.
     */
    private String createLabel(RecordSchemaDescription rsd, boolean isArray) {
        if (isArray)
            return SchemaConverter.Label.RELATIONAL.name();

        if (rsd.getUnique() == Char.TRUE)
            return SchemaConverter.Label.IDENTIFIER.name();

        return null;
    }
}
