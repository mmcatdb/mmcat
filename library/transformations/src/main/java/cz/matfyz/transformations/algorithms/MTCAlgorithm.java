package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
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
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.transformations.exception.InvalidStateException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
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

        final Deque<StackTriple> stack = createStack(rootRecord, rootAccessPath);

        while (!stack.isEmpty())
            processTopOfStack(stack);
    }

    private Deque<StackTriple> createStack(RootRecord rootRecord, ComplexProperty rootAccessPath) {
        final InstanceObjex instanceObjex = instance.getObjex(mapping.rootObjex());

        final var superId = findSuperIdForRoot(rootRecord);
        final DomainRow row = addRow(instanceObjex, superId);
        addDependentValuesToRow(instanceObjex, row, rootRecord, Signature.empty());

        final Deque<StackTriple> stack = new ArrayDeque<>();

        addPathChildrenToStack(stack, rootAccessPath, row, rootRecord);

        return stack;
    }

    // #region Stack processing

    private void processTopOfStack(Deque<StackTriple> stack) {
        final StackTriple triple = stack.pop();
        LOGGER.debug("Process top of stack:\n{}", triple);

        final InstanceObjex childObjex = instance.getObjex(triple.parentToChild.to());

        // There are two cases - either the child objex has a dedicatd access path, or it doesn't.
        if (triple.childAccessPath == null) {
            // If it doesn't, we have to find its values in the parent record.
            final var superId = findSuperIdForNonRoot(triple, childObjex, null);
            DomainRow childRow = addRow(childObjex, superId);
            childRow = addRelation(triple.parentToChild, triple.parentRow, childRow, triple.parentRecord);
            addDependentValuesToRow(childObjex, childRow, triple.parentRecord, triple.parentToChild.signature());

            // TODO probably add values to the domain rows along the path? Or describe why they aren't any.

            return;
        }

        // If it does, we find the values in the child records.
        final var children = triple.parentRecord.getComplexRecords(triple.parentToChild.signature());

        for (final var childRecord : children) {
            final var superId = findSuperIdForNonRoot(triple, childObjex, childRecord);
            DomainRow childRow = addRow(childObjex, superId);
            childRow = addRelation(triple.parentToChild, triple.parentRow, childRow, triple.parentRecord);
            addDependentValuesToRow(childObjex, childRow, childRecord, Signature.empty());

            // TODO probably add values to the domain rows along the path? Or describe why they aren't any.

            addPathChildrenToStack(stack, triple.childAccessPath, childRow, childRecord);
        }
    }

    /**
     * Determine possible sub-paths to be traversed from this complex property (inner node of an access path) and add them to the stack.
     */
    private void addPathChildrenToStack(Deque<StackTriple> stack, ComplexProperty path, DomainRow parentRow, ComplexRecord complexRecord) {
        for (final AccessPath subpath : path.subpaths()) {
            final var signature = subpath.signature();
            if (signature.isEmpty())
                continue;

            final var parentToChild = mapping.category().getPath(signature);

            if (!parentToChild.to().ids().isValue()) {
                if (!(subpath instanceof final ComplexProperty complexSubpath))
                    throw InvalidStateException.simplePropertyForNonValueIds(subpath);

                // The record might not be here - if the data is missing.
                if (complexRecord.hasPrefix(parentToChild.signature()))
                    stack.push(new StackTriple(parentRow, complexRecord, parentToChild, complexSubpath));
                continue;
            }

            // There is no need to explore the value-identified objects - their values will be stored in their parents.
            // However, even though we won't create rows for them, we need to traverse other objects in the path.
            // E.g., if there is a morphism 1.2, the cod object of 1 should still be created.

            if (!signature.isComposite())
                continue;

            final var parentToEntity = mapping.category().getPath(signature.cutLast());

            // Value-identified objects are stored in their parents, we don't need to create a dedicated domain objects for them.
            // So, it doesn't make sense for them to be here.
            if (parentToEntity.to().ids().isValue())
                throw InvalidStateException.complexPropertyForValueIds(parentToEntity.to().key());

            if (complexRecord.hasPrefix(parentToEntity.signature()))
                stack.push(new StackTriple(parentRow, complexRecord, parentToEntity, null));
        }
    }

    // #endregion
    // #region Finding superId

    private SuperIdValues findSuperIdForRoot(RootRecord rootRecord) {
        final var objex = mapping.rootObjex();
        if (objex.ids().isGenerated())
            // If the root objex has a generated id, we generate it now. This is an exception, because we don't normally generate the ids for the auxiliary properties (which the root objex always is).
            return SuperIdValues.fromEmptySignature(idGenerator.next());

        final var builder = new SuperIdValues.Builder();

        for (final Signature signature : objex.superId()) {
            final @Nullable String value = rootRecord.findScalarValue(signature, false);
            // We try to find the whole superId. However, if some parts are not there, we simply skip them.
            if (value != null)
                builder.add(signature, value);
        }

        return builder.build();
    }

    private SuperIdValues findSuperIdForNonRoot(StackTriple triple, InstanceObjex childObjex, @Nullable ComplexRecord childRecord) {
        if (childObjex.schema.ids().isGenerated())
            // The generated id can't be a part of any signature id, so we can't find it in the parent row. By definition, it's not in the record. So we have to generate it.
            return SuperIdValues.fromEmptySignature(idGenerator.next());

        final var builder = new SuperIdValues.Builder();

        final var record = childRecord != null ? childRecord : triple.parentRecord;
        final var isChildRecord = childRecord != null;

        for (final Signature signature : childObjex.schema.superId()) {
            final var value = findSuperIdValue(triple, record, isChildRecord, signature);
            if (value != null)
                builder.add(signature, value);
        }

        return builder.build();
    }

    private @Nullable String findSuperIdValue(StackTriple triple, ComplexRecord record, boolean isChildRecord, Signature signature) {
        // How the signature looks like from the parent objex.
        final var signatureInParent = triple.parentToChild.signature().traverse(signature);
        // If the value is in the parent row, we just add it and move on with our lives.
        final var valueInParent = triple.parentRow.tryFindScalarValue(signatureInParent);
        if (valueInParent != null)
            return valueInParent;

        return isChildRecord
            ? record.findScalarValue(signature, false)
            : record.findScalarValue(signatureInParent, false);
    }

    // #endregion

    /**
     * @param record The record in which the values are searched. Corresponds either to the row itself, or to a parent property in the access path.
     * @param prefix Empty in the first case, path from the parent property in the second case.
     */
    private void addDependentValuesToRow(InstanceObjex objex, DomainRow row, ComplexRecord record, Signature prefix) {
        // The values can only be in the record so there's no need to search elsewhere.
        for (final BaseSignature signature : objex.simpleSignatures()) {
            final String value = record.findScalarValue(prefix.traverse(signature), true);
            if (value != null)
                row.addSimpleValue(signature, value);
        }

        for (final BaseSignature signature : objex.arraySignatures()) {
            final List<String> values = record.findDirectArrayValues(prefix.traverse(signature));
            if (!values.isEmpty())
                row.addArrayValues(signature, values);
        }
    }

    // #region Domain rows

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
        final boolean isNew = values.signatures().stream().anyMatch(signature -> !row.superId.hasSignature(signature));
        if (!isNew)
            // If the row already contains all the values, we can simply return it.
            return row;

        return objex.mergeValues(row, values);
    }

    private DomainRow addRelation(SchemaPath path, DomainRow parentRow, DomainRow childRow, ComplexRecord childRecord) {
        // First, create a domain row with technical id for each objex between the domain and the codomain objexes on the path of the morphism.
        var currentDomainRow = parentRow;

        var parentToCurrent = Signature.empty();
        var currentToChild = path.signature();

        for (final var edge : path.edges()) {
            final var toObjex = edge.to();

            parentToCurrent = parentToCurrent.concatenate(currentToChild.getFirst());
            currentToChild = currentToChild.cutFirst();

            // If we are not at the end of the morphisms, we have to create (or get, if it exists) a new row.
            //if (!toObjex.equals(morphism.cod())) {
            final var values = findSuperIdForRelation(toObjex, parentRow, parentToCurrent.dual(), childRow, currentToChild, childRecord);
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
                return addValuesToRow(instanceObjex, mapping.cod(), values);
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

    private SuperIdValues findSuperIdForRelation(SchemaObjex objex, DomainRow parentRow, Signature pathToParent, DomainRow childRow, Signature pathToChild, ComplexRecord parentRecord) {
        final var builder = new SuperIdValues.Builder();

        for (final var signature : objex.superId()) {
            // The value is in either the first row ...
            final var signatureInFirstRow = pathToParent.traverseBack(signature);
            if (parentRow.superId.hasSignature(signatureInFirstRow)) {
                builder.add(signature, parentRow.superId.getValue(signatureInFirstRow));
                continue;
            }

            final var signatureInLastRow = pathToChild.traverseBack(signature);
            if (childRow.superId.hasSignature(signatureInLastRow))
                builder.add(signature, childRow.superId.getValue(signatureInLastRow));

            // TODO find the value in parent record
        }

        return builder.build();
    }

    // //#endregion

}
