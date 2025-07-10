package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.core.instance.InstanceObjex;
import cz.matfyz.core.instance.SuperIdValues;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.core.record.SimpleRecord;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.UniqueIdGenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME This is still not right.
// Now that there is only one technical id per row, it might happen that a row is replaced (in the domain) while we still have a reference to the now non-existing row (e.g., on the stack above).
// A working solution might be to first create all the rows and then merge them all at once.
// Merging this way might be even quite efficient - we could first merge all rows, then all mappings, then propagate the references, and then continue with a new iteration.

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

        final Deque<StackTriple> masterStack = createStackWithObjex(mapping.rootObjex(), rootRecord, rootAccessPath);

        // processing of the tree
        while (!masterStack.isEmpty())
            processTopOfStack(masterStack);
    }

    private Deque<StackTriple> createStackWithObjex(SchemaObjex objex, RootRecord rootRecord, ComplexProperty rootAccessPath) {
        final InstanceObjex instanceObjex = instance.getObjex(objex);
        // If the root objex has a generated id, we generate it now. This is an exception, because we don't normally generate the ids for the auxiliary properties (which the root objex always is).
        final SuperIdValues values = objex.ids().isGenerated()
            ? SuperIdValues.fromEmptySignature(idGenerator.next())
            : fetchSuperIdValues(objex.superId(), rootRecord);

        final Deque<StackTriple> masterStack = new ArrayDeque<>();

        final DomainRow row = addRow(instanceObjex, values);
        addPathChildrenToStack(masterStack, rootAccessPath, row, rootRecord);

        return masterStack;
    }

    // Fetch id-with-values for given root record.
    private static SuperIdValues fetchSuperIdValues(SignatureId superId, RootRecord rootRecord) {
        final var builder = new SuperIdValues.Builder();

        for (final Signature signature : superId.signatures()) {
            for (final SimpleRecord<?> simpleRecord : rootRecord.findSimpleRecords(signature))
                builder.add(signature, simpleRecord.getValue().toString());
        }

        return builder.build();
    }

    private void processTopOfStack(Deque<StackTriple> masterStack) {
        final StackTriple triple = masterStack.pop();
        LOGGER.debug("Process top of stack:\n{}", triple);
        final var fetchedList = SuperIdValuesFetcher.fetch(idGenerator, triple.parentRecord, triple.parentRow, triple.parentToChild, triple.childAccessPath);

        final InstanceObjex childInstance = instance.getObjex(triple.parentToChild.to());

        for (final var fetched : fetchedList) {
            DomainRow childRow = addRow(childInstance, fetched.superId());
            childRow = addRelation(triple.parentToChild, triple.parentRow, childRow, triple.parentRecord);

            addPathChildrenToStack(masterStack, triple.childAccessPath, childRow, fetched.childRecord());
        }
    }

    private DomainRow addRow(InstanceObjex objex, SuperIdValues values) {
        if (values.tryFindFirstId(objex.schema.ids()) == null)
            // The superId doesn't contain any id, we have to create a technical one.
            return objex.createRowWithTechnicalId(values);

        final var currentRow = objex.tryFindRow(values);
        if (currentRow != null)
            // The row already exists, so we just add the values to it.
            return addValuesToRow(objex, currentRow, values);

        final var newRow = objex.createRowWithValueId(values);
        return objex.mergeReferences(newRow);
    }

    public DomainRow addValuesToRow(InstanceObjex objex, DomainRow row, SuperIdValues values) {
        final boolean isNew = values.signatures().stream().anyMatch(signature -> !row.hasSignature(signature));
        if (!isNew)
            // If the row already contains all the values, we can simply return it.
            return row;

        return objex.mergeValues(row, values);
    }

    private DomainRow addRelation(SchemaPath path, DomainRow parentRow, DomainRow childRow, ComplexRecord childRecord) {
        // First, create a domain row with technical id for each objex between the domain and the codomain objexes on the path of the morphism.
        var currentDomainRow = parentRow;

        var parentToCurrent = Signature.createEmpty();
        var currentToChild = path.signature();

        for (final var edge : path.edges()) {
            final var toObjex = edge.to();

            parentToCurrent = parentToCurrent.concatenate(currentToChild.getFirst());
            currentToChild = currentToChild.cutFirst();

            // If we are not at the end of the morphisms, we have to create (or get, if it exists) a new row.
            //if (!toObjex.equals(morphism.cod())) {
            final var values = fetchSuperIdForRelation(toObjex, parentRow, parentToCurrent.dual(), childRow, currentToChild, childRecord);
            currentDomainRow = addEdge(edge, currentDomainRow, values);
            //}
            /*
            else {
                baseMorphism.createMapping(currentDomainRow, childRow);
                // TODO both rows might need to reference the rows to which they are connected by the baseMorphism. Although it is rare, it should be adressed. See a similar comment in the Merger::createNewMappingsForMorphism() function.
            }
            */
        }

        return currentDomainRow;

        // Now try merging them from the codomain objex to the domain objex (the other way should be already merged).
        //final var merger = new Merger();
        //return merger.mergeAlongMorphism(childRow, baseMorphisms.get(baseMorphisms.size() - 1).dual());
    }

    private DomainRow addEdge(SchemaEdge edgeToObjex, DomainRow fromRow, SuperIdValues values) {
        final var instanceObjex = instance.getObjex(edgeToObjex.to());

        // First, we try to find it by the connection.
        if (edgeToObjex.direction()) {
            // The edge doesn't go against the morphism so there can be at most one mapping.
            final var mapping = fromRow.getMappingFrom(edgeToObjex.signature());
            if (mapping != null)
                return addValuesToRow(instanceObjex, mapping.codomainRow(), values);
        }

        // Then we try to find the row by the superId values.
        final var currentRow = instanceObjex.tryFindRow(values);
        if (currentRow != null) {
            if (!fromRow.hasMappingToOther(currentRow, edgeToObjex)) {
                // The connection does not exist yet, so we create it and then merge it.
                // TODO optimization - merging with the knowledge of the connection, so we would not have create it, then delete it and then create it for the new row.
                InstanceMorphism.createMappingForEdge(instance, edgeToObjex, fromRow, currentRow);
            }

            return addValuesToRow(instanceObjex, currentRow, values);
        }

        // No such row exists yet, so we have to create it. It also don't have to be merged so we are not doing that.
        final var newRow = instanceObjex.createRow(values);
        InstanceMorphism.createMappingForEdge(instance, edgeToObjex, fromRow, newRow);

        return instanceObjex.mergeReferences(newRow);
    }

    private SuperIdValues fetchSuperIdForRelation(SchemaObjex objex, DomainRow parentRow, Signature pathToParent, DomainRow childRow, Signature pathToChild, ComplexRecord parentRecord) {
        final var builder = new SuperIdValues.Builder();

        for (final var signature : objex.superId().signatures()) {
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
