package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.record.DynamicRecordName;
import cz.cuni.matfyz.core.record.DynamicRecordWrapper;
import cz.cuni.matfyz.core.record.IComplexRecord;
import cz.cuni.matfyz.core.record.SimpleArrayRecord;
import cz.cuni.matfyz.core.record.SimpleRecord;
import cz.cuni.matfyz.core.record.SimpleValueRecord;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;
import cz.cuni.matfyz.transformations.exception.TransformationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SuperIdsFetcher {
    /**
     * Fetch id-with-values for a schema object in given record / domain row.
     * The output is a set of (Signature, String) for each Signature in superId and its corresponding value from record. Actually, there can be multiple values in the record, so a list of these sets is returned.
     * For further processing, the child records associated with the values are needed (if they are complex), so they are added to the output as well.
     * @param parentRecord Record of the parent (in the access path) schema object.
     * @param parentRow Domain row of the parent schema object.
     * @param morphism Morphism from the parent schema object to the currently processed one.
     * @return
     */
    public static Iterable<FetchedSuperId> fetch(IComplexRecord parentRecord, DomainRow parentRow, InstanceMorphism morphism, AccessPath childAccessPath) {
        final var fetcher = new SuperIdsFetcher(parentRow, morphism, childAccessPath);
        fetcher.process(parentRecord);

        return fetcher.output;
    }

    public record FetchedSuperId(SuperIdWithValues superId, IComplexRecord childRecord) {}

    final List<FetchedSuperId> output;
    final DomainRow parentRow;
    final Signature parentToChild;
    final InstanceObject childObject;
    final AccessPath childAccessPath;

    private SuperIdsFetcher(DomainRow parentRow, InstanceMorphism morphism, AccessPath childAccessPath) {
        this.output = new ArrayList<>();
        this.parentRow = parentRow;
        this.parentToChild = morphism.signature();
        this.childObject = morphism.cod();
        this.childAccessPath = childAccessPath;
    }

    private void process(IComplexRecord parentRecord) {
        if (childObject.ids().isGenerated()) {
            // If the id is generated, we have to generate it now.
            // It's not possible for any record to have a clue about the value of this id, and this is also the only id of the child row.
            if (childAccessPath instanceof ComplexProperty) {
                // It has to be a nested document (not auxiliary).
                if (parentRecord.hasComplexRecords(parentToChild)) {
                    // There are complex records with given signature in the parent record.
                    // They don't represent any (string) value so an unique identifier must be generated instead.
                    // But their complex value will be processed later.
                    for (IComplexRecord childRecord : parentRecord.getComplexRecords(parentToChild))
                        addSimpleValueWithChildRecordToOutput(UniqueIdProvider.getNext(), childRecord);
                }
            }
            else {
                addSimpleValueToOutput(UniqueIdProvider.getNext());
            }
        }
        else if (childObject.ids().isValue()) {
            // An object identified by its value has to be a simple value object.
            // The output will have only one tuple: (<signature>, <value>).
            if (parentRow.hasSignature(parentToChild)) {
                // Value is in the parent domain row.
                String valueFromParentRow = parentRow.getValue(parentToChild);
                addSimpleValueToOutput(valueFromParentRow);
            }
            else if (parentRecord.hasSimpleRecord(parentToChild)) {
                // There is simple value/array record with given signature in the parent record.
                addSuperIdsFromSimpleRecordToOutput(parentRecord.getSimpleRecord(parentToChild));
            }
        }
        else if (parentRecord.hasComplexRecords(parentToChild)) {
            // The superId isn't empty so we need to find value for each signature in superId and return the tuples (<signature>, <value>).
            // Because there are multiple signatures in the superId, we are dealing with a complex property (resp. properties, i.e., children of given parentRecord).
            for (IComplexRecord childRecord : parentRecord.getComplexRecords(parentToChild))
                processComplexRecord(childRecord);
        }
    }

    private void addSuperIdsFromSimpleRecordToOutput(SimpleRecord<?> simpleRecord) {
        if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
            addSimpleValueToOutput(simpleValueRecord.getValue().toString());
        else if (simpleRecord instanceof SimpleArrayRecord<?> simpleArrayRecord)
            simpleArrayRecord.getValues().stream()
                .forEach(valueObject -> addSimpleValueToOutput(valueObject.toString()));
    }

    private void addSimpleValueToOutput(String value) {
        // It doesn't matter if there is null because the accessPath is also null so it isn't further traversed
        addSimpleValueWithChildRecordToOutput(value, null);
    }

    private void addSimpleValueWithChildRecordToOutput(String value, IComplexRecord childRecord) {
        var builder = new SuperIdWithValues.Builder();
        builder.add(Signature.createEmpty(), value);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private void processComplexRecord(IComplexRecord childRecord) {
        // If the record has children/values with dynamic names for a signature, it is not allowed to have any other children/values (even with static names) for any other signature.
        // So there are two different complex records - one with static children/values (with possibly different signatures) and the other with only dynamic ones (with the same signature).
        if (childRecord.hasDynamicNameChildren())
            processComplexRecordWithDynamicChildren(childRecord);
        else if (childRecord.hasDynamicNameValues())
            processComplexRecordWithDynamicValues(childRecord);
        else
            processStaticComplexRecord(childRecord);
    }

    private void processComplexRecordWithDynamicChildren(IComplexRecord childRecord) {
        for (IComplexRecord dynamicNameChild : childRecord.getDynamicNameChildren()) {
            var builder = new SuperIdWithValues.Builder();
            addStaticNameSignaturesToBuilder(builder, childObject.superId().signatures(), dynamicNameChild);
            output.add(new FetchedSuperId(builder.build(), new DynamicRecordWrapper(childRecord, dynamicNameChild)));
        }
    }

    private void processComplexRecordWithDynamicValues(IComplexRecord childRecord) {
        for (SimpleValueRecord<?> dynamicNameValue : childRecord.getDynamicNameValues()) {
            var builder = new SuperIdWithValues.Builder();
            Set<Signature> staticNameSignatures = new TreeSet<>();

            for (Signature signature : childObject.superId().signatures()) {
                if (dynamicNameValue.signature().equals(signature))
                    builder.add(signature, dynamicNameValue.getValue().toString());
                else if (dynamicNameValue.name() instanceof DynamicRecordName dynamicName && dynamicName.signature().equals(signature))
                    builder.add(signature, dynamicNameValue.name().value());
                // If the signature is not the dynamic value nor its dynamic name, it is static and we have to find it elsewhere.
                else
                    staticNameSignatures.add(signature);
            }

            addStaticNameSignaturesToBuilder(builder, staticNameSignatures, childRecord);

            output.add(new FetchedSuperId(builder.build(), childRecord));
        }
    }
    
    private void processStaticComplexRecord(IComplexRecord childRecord) {
        var builder = new SuperIdWithValues.Builder();
        addStaticNameSignaturesToBuilder(builder, childObject.superId().signatures(), childRecord);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private void addStaticNameSignaturesToBuilder(SuperIdWithValues.Builder builder, Set<Signature> signatures, IComplexRecord childRecord) {
        for (Signature signature : signatures) {
            // How the signature looks like from the parent object.
            var signatureInParentRow = signature.traverseThrough(parentToChild);

            // Why java still doesn't support output arguments?
            String value;
            if (signatureInParentRow == null) {
                SimpleRecord<?> simpleRecord = childRecord.getSimpleRecord(signature);
                if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
                    value = simpleValueRecord.getValue().toString();
                else if (childRecord.name() instanceof DynamicRecordName dynamicName && dynamicName.signature().equals(signature))
                    value = dynamicName.value();
                else
                    throw new TransformationException("FetchSuperIds doesn't support array values for complex records.");
            }
            else
                value = parentRow.getValue(signatureInParentRow);

            builder.add(signature, value);
        }
    }
}
