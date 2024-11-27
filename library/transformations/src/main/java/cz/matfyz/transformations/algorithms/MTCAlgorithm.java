package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.instance.SuperIdWithValues;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.core.record.SimpleRecord;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.UniqueIdGenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MTCAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithm.class);

    public static void run(Mapping mapping, InstanceCategory instance, ForestOfRecords forest) {
        new MTCAlgorithm(mapping, instance, forest).run();
    }

    private final ForestOfRecords forest;
    private final Mapping mapping;
    private final InstanceCategory instance;

    private MTCAlgorithm(Mapping mapping, InstanceCategory instance, ForestOfRecords forest) {
        this.forest = forest;
        this.mapping = mapping;
        this.instance = instance;
    }

    private final UniqueIdGenerator idGenerator = UniqueIdGenerator.create();

    private void run() {
        LOGGER.debug("Model To Category algorithm");
        final ComplexProperty rootAccessPath = mapping.accessPath()
            // The auxiliary nodes are merged with their parents during the pull forest algorithm.
            .copyWithoutAuxiliaryNodes()
            // The dynamic names are replaced by a complex records with name and value properties during the pull forest algorithm.
            .copyWithoutDynamicNames().path();

        // Create references for adding found values to the superId of other rows.
        instance.createReferences();
        for (RootRecord rootRecord : forest)
            processRootRecord(rootRecord, rootAccessPath);
    }

    private void processRootRecord(RootRecord rootRecord, ComplexProperty rootAccessPath) {
        LOGGER.debug("Process a root record:\n{}", rootRecord);

        final Deque<StackTriple> masterStack = createStackWithObject(mapping.rootObject(), rootRecord, rootAccessPath);

        // processing of the tree
        while (!masterStack.isEmpty())
            processTopOfStack(masterStack);
    }

    private Deque<StackTriple> createStackWithObject(SchemaObject object, RootRecord rootRecord, ComplexProperty rootAccessPath) {
        final InstanceObject instanceObject = instance.getObject(object);
        // If the root object has a generated id, we generate it now. This is an exception, because we don't normally generate the ids for the auxiliary properties (which the root object always is).
        final SuperIdWithValues superId = object.ids().isGenerated()
            ? SuperIdWithValues.fromEmptySignature(idGenerator.next())
            : fetchSuperId(object.superId(), rootRecord);

        final Deque<StackTriple> masterStack = new ArrayDeque<>();

        final DomainRow row = instanceObject.getOrCreateRow(superId);
        addPathChildrenToStack(masterStack, rootAccessPath, row, rootRecord);

        return masterStack;
    }

    // Fetch id-with-values for given root record.
    private static SuperIdWithValues fetchSuperId(SignatureId superId, RootRecord rootRecord) {
        final var builder = new SuperIdWithValues.Builder();

        for (final Signature signature : superId.signatures()) {
            for (final SimpleRecord<?> simpleRecord : rootRecord.findSimpleRecords(signature))
                builder.add(signature, simpleRecord.getValue().toString());
        }

        return builder.build();
    }

    private void processTopOfStack(Deque<StackTriple> masterStack) {
        final StackTriple triple = masterStack.pop();
        LOGGER.debug("Process top of stack:\n{}", triple);
        final var superIds = SuperIdsFetcher.fetch(idGenerator, triple.parentRecord, triple.parentRow, triple.parentToChild, triple.childAccessPath);

        final InstanceObject childInstance = instance.getObject(triple.parentToChild.to());

        for (final var superId : superIds) {
            DomainRow childRow = childInstance.getOrCreateRow(superId.superId());
            childRow = addRelation(triple.parentToChild, triple.parentRow, childRow, triple.parentRecord);

            //childInstance.merge(childRow);

            addPathChildrenToStack(masterStack, triple.childAccessPath, childRow, superId.childRecord());
        }
    }

    private DomainRow addRelation(SchemaPath path, DomainRow parentRow, DomainRow childRow, ComplexRecord childRecord) {
        // First, create a domain row with technical id for each object between the domain and the codomain objects on the path of the morphism.
        var currentDomainRow = parentRow;

        var parentToCurrent = Signature.createEmpty();
        var currentToChild = path.signature();

        for (final var edge : path.edges()) {
            final var instanceObject = edge.to();

            parentToCurrent = parentToCurrent.concatenate(currentToChild.getFirst());
            currentToChild = currentToChild.cutFirst();

            // If we are not at the end of the morphisms, we have to create (or get, if it exists) a new row.
            //if (!instanceObject.equals(morphism.cod())) {
            final var superId = fetchSuperIdForTechnicalRow(instanceObject, parentRow, parentToCurrent.dual(), childRow, currentToChild, childRecord);
            currentDomainRow = InstanceObject.getOrCreateRowWithEdge(instance, superId, currentDomainRow, edge);
            //}
            /*
            else {
                baseMorphism.createMapping(currentDomainRow, childRow);
                // TODO both rows might need to reference the rows to which they are connected by the baseMorphism. Although it is rare, it should be adressed. See a similar comment in the Merger::createNewMappingsForMorphism() function.
            }
            */
        }

        return currentDomainRow;

        // Now try merging them from the codomain object to the domain object (the other way should be already merged).
        //final var merger = new Merger();
        //return merger.mergeAlongMorphism(childRow, baseMorphisms.get(baseMorphisms.size() - 1).dual());
    }

    private SuperIdWithValues fetchSuperIdForTechnicalRow(SchemaObject object, DomainRow parentRow, Signature pathToParent, DomainRow childRow, Signature pathToChild, ComplexRecord parentRecord) {
        final var builder = new SuperIdWithValues.Builder();

        for (final var signature : object.superId().signatures()) {
            // The value is in either the first row ...
            final var signatureInFirstRow = signature.traverseAlong(pathToParent);
            if (parentRow.hasSignature(signatureInFirstRow)) {
                builder.add(signature, parentRow.getValue(signatureInFirstRow));
                continue;
            }

            final var signatureInLastRow = signature.traverseAlong(pathToChild);
            if (childRow.hasSignature(signatureInLastRow))
                builder.add(signature, childRow.getValue(signatureInLastRow));

            // TODO find the value in parent record
        }

        return builder.build();
    }

    private record Child(Signature signature, AccessPath property) {}

    private void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, DomainRow parentRow, ComplexRecord complexRecord) {
        if (!(path instanceof final ComplexProperty complexPath))
            return;

        for (final Child child : children(complexPath)) {
            if (child.signature.isEmpty())
                continue;

            final var parentToChild = mapping.category().getPath(child.signature());
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

        for (final AccessPath subpath : complexProperty.subpaths())
            output.add(new Child(subpath.signature(), subpath));

        return output;
    }

}
