package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.SuperIdValues;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.SimpleRecord;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.transformations.exception.InvalidStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

public class SuperIdValuesFetcher {
    /**
     * Fetch id-with-values for a schema objex in given record / domain row.
     * The output is a set of (Signature, String) for each Signature in superId and its corresponding value from record. Actually, there can be multiple values in the record, so a list of these sets is returned.
     * For further processing, the child records associated with the values are needed (if they are complex), so they are added to the output as well.
     * @param parentRecord Record of the parent (in the access path) schema objex.
     * @param parentRow Domain row of the parent schema objex.
     * @param morphism Morphism from the parent schema objex to the currently processed one.
     * @return
     */
    public static Iterable<FetchedSuperIdValues> fetch(UniqueIdGenerator idGenerator, ComplexRecord parentRecord, DomainRow parentRow, SchemaPath path, AccessPath childAccessPath) {
        final var fetcher = new SuperIdValuesFetcher(idGenerator, parentRow, path, childAccessPath);
        fetcher.process(parentRecord);

        return fetcher.output;
    }

    public record FetchedSuperIdValues(SuperIdValues superId, ComplexRecord childRecord) {}

    private final UniqueIdGenerator idGenerator;
    private final List<FetchedSuperIdValues> output;
    private final DomainRow parentRow;
    private final Signature parentToChild;
    private final SchemaObjex childObjex;
    private final AccessPath childAccessPath;

    private SuperIdValuesFetcher(UniqueIdGenerator idGenerator, DomainRow parentRow, SchemaPath path, AccessPath childAccessPath) {
        this.idGenerator = idGenerator;
        this.output = new ArrayList<>();
        this.parentRow = parentRow;
        this.parentToChild = path.signature();
        this.childObjex = path.to();
        this.childAccessPath = childAccessPath;
    }

    private void process(ComplexRecord parentRecord) {
        if (childObjex.ids().isGenerated()) {
            // If the id is generated, we have to generate it now.
            // It's not possible for any record to have any clue about the value of this id, and this is also the only id of the child row.
            if (childAccessPath instanceof ComplexProperty) {
                // Now we are a nested document (not auxiliary).

                // If there are complex records with given signature in the parent record, we have to process them.
                // They don't represent any (string) value so an unique identifier must be generated instead.
                // But their complex value will be processed later.
                final var children = parentRecord.getComplexRecords(parentToChild);
                if (children != null)
                    children.stream().forEach(childRecord -> addSimpleWithChildRecordToOutput(idGenerator.next(), childRecord));
            }
            else {
                addSimpleToOutput(idGenerator.next());
            }
        }
        else if (childObjex.ids().isValue()) {
            // An objex identified by its value has to be a simple value objex.
            // The output will have only one tuple: (<signature>, <value>).
            if (parentRow.hasSignature(parentToChild)) {
                // Value is in the parent domain row.
                final String valueFromParentRow = parentRow.getValue(parentToChild);
                addSimpleToOutput(valueFromParentRow);
            }
            else if (parentRecord.getSimpleRecords(parentToChild) != null) {
                // There is simple value/array record with given signature in the parent record.
                addSuperIdsFromSimpleRecordToOutput(parentRecord.getSimpleRecords(parentToChild));
            }
        }
        else {
            // The superId isn't empty so we need to find value for each signature in superId and return the tuples (<signature>, <value>).
            // Because there are multiple signatures in the superId, we are dealing with a complex property (resp. properties, i.e., children of given parentRecord).
            final var children = parentRecord.getComplexRecords(parentToChild);
            if (children != null)
                children.stream().forEach(this::processComplexRecord);
        }
    }

    private void addSuperIdsFromSimpleRecordToOutput(List<SimpleRecord<?>> simpleRecord) {
        simpleRecord.stream().forEach(valueObject -> addSimpleToOutput(valueObject.getValue().toString()));
    }

    private void addSimpleToOutput(String value) {
        // It doesn't matter if there is null because the accessPath is also null so it isn't further traversed.
        addSimpleWithChildRecordToOutput(value, null);
    }

    private void addSimpleWithChildRecordToOutput(String value, ComplexRecord childRecord) {
        output.add(new FetchedSuperIdValues(SuperIdValues.fromEmptySignature(value), childRecord));
    }

    private void processComplexRecord(ComplexRecord childRecord) {
        final var builder = new SuperIdValues.Builder();
        addStringNameSignaturesToBuilder(builder, childObjex.superId().signatures(), childRecord);
        output.add(new FetchedSuperIdValues(builder.build(), childRecord));
    }

    private void addStringNameSignaturesToBuilder(SuperIdValues.Builder builder, Set<Signature> signatures, ComplexRecord childRecord) {
        for (final Signature signature : signatures) {
            // How the signature looks like from the parent objex.
            final var signatureInParentRow = signature.traverseThrough(parentToChild);
            if (signatureInParentRow != null) {
                // If the value is in the parent row, we just add it and move on with our lives.
                builder.add(signature, parentRow.getValue(signatureInParentRow));
                continue;
            }

            final @Nullable List<SimpleRecord<?>> simpleRecords = childRecord.getSimpleRecords(signature);
            if (simpleRecords == null)
                continue;

            if (simpleRecords.size() != 1)
                throw InvalidStateException.superIdValuesHasArrayValue();

            builder.add(signature, simpleRecords.get(0).getValue().toString());
        }
    }

}
