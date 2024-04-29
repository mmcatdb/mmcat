package cz.matfyz.core.record;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.printable.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents an inner node of the record tree.
 * The value of this record are its children.
 */
public class ComplexRecord extends DataRecord implements IComplexRecord, Printable {

    private final Map<Signature, List<ComplexRecord>> children = new TreeMap<>();
    private final List<ComplexRecord> dynamicNameChildren = new ArrayList<>();
    private Signature dynamicNameSignature;
    private final Map<Signature, SimpleRecord<?>> values = new TreeMap<>();
    private final List<SimpleValueRecord<?>> dynamicNameValues = new ArrayList<>();

    protected ComplexRecord(RecordName name) {
        super(name);
    }

    public boolean hasComplexRecords(Signature signature) {
        return children.containsKey(signature);
    }

    public List<? extends IComplexRecord> getComplexRecords(Signature signature) {
        return children.get(signature);
    }

    public boolean hasDynamicNameChildren() {
        return !dynamicNameChildren.isEmpty();
    }

    public List<? extends IComplexRecord> getDynamicNameChildren() {
        return dynamicNameChildren;
    }

    public Signature dynamicNameSignature() {
        return dynamicNameSignature;
    }

    public boolean hasSimpleRecord(Signature signature) {
        return values.containsKey(signature);
    }

    public SimpleRecord<?> getSimpleRecord(Signature signature) {
        return values.get(signature);
    }

    public SimpleRecord<?> findSimpleRecord(Signature signature) {
        final var directSimpleRecord = getSimpleRecord(signature);
        if (directSimpleRecord != null)
            return directSimpleRecord;

        final var auxiliaryChildren = children.get(Signature.createEmpty());
        if (auxiliaryChildren != null && auxiliaryChildren.size() == 1)
            return auxiliaryChildren.get(0).findSimpleRecord(signature);

        // There is no hope to find the simple record in the children because that would require at least two-part signature (one base to find the child and one to find the record in it)
        if (signature instanceof BaseSignature)
            return null;

        var currentPath = Signature.createEmpty();
        for (final var base : signature.toBases()) {
            currentPath = currentPath.concatenate(base);
            final var childRecords = children.get(currentPath);

            if (childRecords == null)
                continue;
            if (childRecords.size() != 1)
                return null;

            return childRecords.get(0).findSimpleRecord(signature.traverseAlong(currentPath));
        }

        return null;
    }

    public boolean hasDynamicNameValues() {
        return !dynamicNameValues.isEmpty();
    }

    public List<SimpleValueRecord<?>> getDynamicNameValues() {
        return dynamicNameValues;
    }

    public boolean containsDynamicNameValue(Signature signature) {
        if (!hasDynamicNameValues())
            return false;

        SimpleValueRecord<?> firstDynamicNameValue = dynamicNameValues.get(0);
        return signature.equals(firstDynamicNameValue.signature)
            || firstDynamicNameValue.name() instanceof DynamicRecordName dynamicName && signature.equals(dynamicName.signature());
    }

    public ComplexRecord addComplexRecord(RecordName name, Signature signature) {
        ComplexRecord complexRecord = new ComplexRecord(name);

        if (complexRecord.name instanceof StaticRecordName) {
            List<ComplexRecord> childSet = children.computeIfAbsent(signature, x -> new ArrayList<>());
            childSet.add(complexRecord);
        }
        else {
            dynamicNameChildren.add(complexRecord);
            dynamicNameSignature = signature;
        }

        return complexRecord;
    }

    public <T> SimpleArrayRecord<T> addSimpleArrayRecord(RecordName name, Signature signature, List<T> values) {
        var simpleArrayRecord = new SimpleArrayRecord<>(name, signature, values);
        this.values.put(signature, simpleArrayRecord);

        return simpleArrayRecord;
    }

    public <T> SimpleValueRecord<T> addSimpleValueRecord(RecordName name, Signature signature, T value) {
        var simpleValueRecord = new SimpleValueRecord<>(name, signature, value);

        if (name instanceof StaticRecordName)
            values.put(signature, simpleValueRecord);
        else if (name instanceof DynamicRecordName) {
            dynamicNameValues.add(simpleValueRecord);
        }

        return simpleValueRecord;
    }

    @Override public void printTo(Printer printer) {
        printer.append("{").down().nextLine();

        for (SimpleRecord<?> value : values.values())
            printer.append(value).append(",").nextLine();

        for (SimpleValueRecord<?> dynamicNameValue : dynamicNameValues)
            printer.append(dynamicNameValue).append(",").nextLine();

        for (List<ComplexRecord> list : children.values()) {
            ComplexRecord firstItem = list.get(0);

            if (list.size() > 1) {
                printer.append(firstItem.name).append(": ");
                printer.append("[").down().nextLine();
                printer.append(firstItem);

                for (int i = 1; i < list.size(); i++)
                    printer.append(",").nextLine().append(list.get(i));

                printer.up().nextLine().append("]");
            }
            // Normal complex property
            else {
                printer.append(firstItem.name).append(": ").append(firstItem);
            }

            printer.append(",").nextLine();
        }

        for (ComplexRecord dynamicNameChild : dynamicNameChildren) {
            printer.append(dynamicNameChild.name).append(": ").append(dynamicNameChild).append(",").nextLine();
        }

        printer.remove().up().nextLine().append("}");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    @Override public boolean equals(Object object) {
        if (object == this)
            return true;

        if (!(object instanceof ComplexRecord complexRecord))
            return false;

        return this.toString().equals(complexRecord.toString());
    }
}
