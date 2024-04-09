package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategory.InstancePath;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.instance.SuperIdWithValues;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.IComplexRecord;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.core.record.SimpleRecord;
import cz.matfyz.core.record.SimpleValueRecord;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class MTCAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithm.class);

    private ForestOfRecords forest;
    private Mapping mapping;
    private InstanceCategory category;

    public void input(Mapping mapping, InstanceCategory category, ForestOfRecords forest) {
        this.forest = forest;
        this.mapping = mapping;
        this.category = category;
    }

    public void algorithm() {
        System.out.println("In MTCAlgo()");
        LOGGER.debug("Model To Category algorithm");
        final ComplexProperty rootAccessPath = mapping.accessPath().copyWithoutAuxiliaryNodes();

        // Create references for adding found values to the superId of other rows.
        category.createReferences();
        System.out.println("Size of forest: " + forest.size());
        for (RootRecord rootRecord : forest) {
            System.out.println("root Record" + rootRecord);
            processRootRecord(rootRecord, rootAccessPath);
        }
    }

    private void processRootRecord(RootRecord rootRecord, ComplexProperty rootAccessPath) {
        System.out.println("Processing root recod");
        LOGGER.debug("Process a root record:\n{}", rootRecord);

        Deque<StackTriple> masterStack = createStackWithObject(mapping.rootObject(), rootRecord, rootAccessPath);

        // processing of the tree
        while (!masterStack.isEmpty())
            processTopOfStack(masterStack);
    }

    private Deque<StackTriple> createStackWithObject(SchemaObject object, RootRecord rootRecord, ComplexProperty rootAccessPath) {
        InstanceObject instanceObject = category.getObject(object);
        SuperIdWithValues superId = fetchSuperId(object.superId(), rootRecord);
        Deque<StackTriple> masterStack = new ArrayDeque<>();

        DomainRow row = instanceObject.getOrCreateRow(superId);
        addPathChildrenToStack(masterStack, rootAccessPath, row, rootRecord);

        return masterStack;
    }

    private void processTopOfStack(Deque<StackTriple> masterStack) {
        System.out.println("Processing top of stack");
        StackTriple triple = masterStack.pop();
        final var superIds = SuperIdsFetcher.fetch(triple.parentRecord, triple.parentRow, triple.parentToChild, triple.childAccessPath);

        InstanceObject childInstance = triple.parentToChild.cod();

        for (final var superId : superIds) {
            DomainRow childRow = childInstance.getOrCreateRow(superId.superId());
            childRow = addRelation(triple.parentToChild, triple.parentRow, childRow, triple.parentRecord);

            //childInstance.merge(childRow);

            addPathChildrenToStack(masterStack, triple.childAccessPath, childRow, superId.childRecord());
        }
    }

    // Fetch id-with-values for given root record.
    private static SuperIdWithValues fetchSuperId(SignatureId superId, RootRecord rootRecord) {
        var builder = new SuperIdWithValues.Builder();

        for (Signature signature : superId.signatures()) {
            /*
            Object value = rootRecord.values().get(signature).getValue();
            if (value instanceof String stringValue)
                builder.add(signature, stringValue);
            */
            SimpleRecord<?> simpleRecord = rootRecord.findSimpleRecord(signature);
            if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
                builder.add(signature, simpleValueRecord.getValue().toString());
            // else
            //     throw InvalidStateException.simpleRecordIsNotValue(simpleRecord);
        }

        return builder.build();
    }

    private DomainRow addRelation(InstancePath path, DomainRow parentRow, DomainRow childRow, IComplexRecord childRecord) {
        // First, create a domain row with technical id for each object between the domain and the codomain objects on the path of the morphism.
        var currentDomainRow = parentRow;

        var parentToCurrent = Signature.createEmpty();
        var currentToChild = path.signature();

        for (final var edge : path.edges()) {
            var instanceObject = edge.cod();

            parentToCurrent = parentToCurrent.concatenate(currentToChild.getFirst());
            currentToChild = currentToChild.cutFirst();

            // If we are not at the end of the morphisms, we have to create (or get, if it exists) a new row.
            //if (!instanceObject.equals(morphism.cod())) {
            final var superId = fetchSuperIdForTechnicalRow(instanceObject, parentRow, parentToCurrent.dual(), childRow, currentToChild, childRecord);
            currentDomainRow = InstanceObject.getOrCreateRowWithEdge(superId, currentDomainRow, edge);
            //}
            /*
            else {
                baseMorphism.createMapping(currentDomainRow, childRow);
                // TODO both rows might need to reference the rows to which they are connected by the baseMorphism. Although it is rare, it should be adressed. See a similar comment in the Merger::createNewMappingsForMorphism() function.
            }
            */
        }
        System.out.println("current domain: " + currentDomainRow);
        return currentDomainRow;

        // Now try merging them from the codomain object to the domain object (the other way should be already merged).
        //final var merger = new Merger();
        //return merger.mergeAlongMorphism(childRow, baseMorphisms.get(baseMorphisms.size() - 1).dual());
    }

    private SuperIdWithValues fetchSuperIdForTechnicalRow(InstanceObject instanceObject, DomainRow parentRow, Signature pathToParent, DomainRow childRow, Signature pathToChild, IComplexRecord parentRecord) {
        var builder = new SuperIdWithValues.Builder();

        for (var signature : instanceObject.superId().signatures()) {
            // The value is in either the first row ...
            var signatureInFirstRow = signature.traverseAlong(pathToParent);
            if (parentRow.hasSignature(signatureInFirstRow)) {
                builder.add(signature, parentRow.getValue(signatureInFirstRow));
                continue;
            }

            var signatureInLastRow = signature.traverseAlong(pathToChild);
            if (childRow.hasSignature(signatureInLastRow))
                builder.add(signature, childRow.getValue(signatureInLastRow));

            // TODO find the value in parent record
        }

        return builder.build();
    }

    private record Child(Signature signature, AccessPath property) {}

    private void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, DomainRow parentRow, IComplexRecord complexRecord) {
        if (!(path instanceof ComplexProperty complexPath))
            return;

        for (Child child : children(complexPath)) {
            if (child.signature.isEmpty())
                continue;

            final var parentToChild = category.getPath(child.signature());
            stack.push(new StackTriple(parentRow, parentToChild, child.property(), complexRecord));
        }
    }

    /**
     * Determine possible sub-paths to be traversed from this complex property (inner node of an access path).
     * According to the paper, this function should return pairs of (context, value). But value of such sub-path can only be an {@link ComplexProperty}.
     * Similarly, context must be a signature of a morphism.
     * @return set of pairs (morphism signature, complex property) of all possible sub-paths.
     */
    private static Collection<Child> children(ComplexProperty complexProperty) {
        final List<Child> output = new ArrayList<>();

        for (AccessPath subpath : complexProperty.subpaths()) {
            if (subpath.name() instanceof DynamicName dynamicName)
                output.add(new Child(dynamicName.signature(), ComplexProperty.createEmpty()));

            output.add(new Child(subpath.signature(), subpath));
        }

        return output;
    }

}
