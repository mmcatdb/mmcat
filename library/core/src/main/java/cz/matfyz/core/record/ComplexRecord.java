package cz.matfyz.core.record;

import cz.matfyz.core.exception.SignatureException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.printable.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents an inner node of the record tree. Something like object in JSON.
 */
public class ComplexRecord implements Printable {

    /** The signatures can't be empty. */
    private final Map<Signature, List<ComplexRecord>> children = new TreeMap<>();
    /** The signatures can't be empty. */
    private final Map<Signature, List<SimpleRecord<?>>> values = new TreeMap<>();

    protected ComplexRecord() {}

    public @Nullable List<ComplexRecord> getComplexRecords(Signature signature) {
        return children.get(signature);
    }

    public <TDataType> @Nullable List<TDataType> findValues(Signature signature, boolean onlyDirect) {
        final var records = onlyDirect ? values.get(signature) : findSimpleRecords(signature);
        if (records == null)
            return null;

        final var values = new ArrayList<TDataType>();
        for (final var record : records)
            values.add((TDataType) record.getValue());

        return values;
    }

    public <TDataType> @Nullable TDataType findScalarValue(Signature signature, boolean onlyDirect) {
        final @Nullable List<TDataType> values = findValues(signature, onlyDirect);
        if (values == null)
            return null;

        assert values.size() == 1 : "There should be exactly one value for a signature in a complex record, but found: " + values.size();

        return values.get(0);
    }

    public <TDataType> List<TDataType> findDirectArrayValues(Signature signature) {
        final @Nullable List<TDataType> values = findValues(signature, true);
        return values == null ? List.of() : values;
    }

    private @Nullable List<SimpleRecord<?>> findSimpleRecords(Signature signature) {
        final var directSimpleRecord = values.get(signature);
        if (directSimpleRecord != null)
            return directSimpleRecord;

        // There is no hope to find the simple record in the children because that would require at least two-part signature (one base to find the child and one to find the record in it).
        if (signature instanceof BaseSignature)
            return null;

        var currentPath = Signature.empty();
        for (final var base : signature.toBases()) {
            currentPath = currentPath.concatenate(base);
            final var childRecords = children.get(currentPath);

            if (childRecords == null)
                continue;
            if (childRecords.size() != 1)
                return null;

            return childRecords.get(0).findSimpleRecords(currentPath.traverseBack(signature));
        }

        return null;
    }

    public boolean hasPrefix(Signature prefix) {
        for (final Signature signature : values.keySet()) {
            if (signature.hasPrefix(prefix))
                return true;
        }

        for (final Signature signature : children.keySet()) {
            if (signature.hasPrefix(prefix))
                return true;
        }

        return false;
    }

    public ComplexRecord addComplexRecord(Signature signature) {
        // If the signature is empty, the property is auxiliary.
        // So we skip it and move all its childrens' values to the parent record.
        if (signature.isEmpty())
            return this;

        final ComplexRecord complexRecord = new ComplexRecord();
        children.computeIfAbsent(signature, x -> new ArrayList<>()).add(complexRecord);

        return complexRecord;
    }

    public <TDataType> SimpleRecord<TDataType> addSimpleRecord(Signature signature, TDataType value) {
        if (signature.isEmpty())
            throw SignatureException.isEmpty();

        final var simpleRecord = new SimpleRecord<>(value);
        values.computeIfAbsent(signature, x -> new ArrayList<>()).add(simpleRecord);

        return simpleRecord;
    }

    public ComplexRecord addDynamicReplacer(Signature signature, Signature nameSignature, String name) {
        if (signature.isEmpty())
            throw SignatureException.isEmpty();

        final ComplexRecord dynamicWrapper = new ComplexRecord();
        children.computeIfAbsent(signature, x -> new ArrayList<>()).add(dynamicWrapper);

        dynamicWrapper.addSimpleRecord(nameSignature, name);

        return dynamicWrapper;
    }

    @Override public void printTo(Printer printer) {
        printer.append("{").down().nextLine();

        for (final Signature signature : values.keySet()) {
            final List<SimpleRecord<?>> list = values.get(signature);
            printer.append("[").append(signature).append("]: [");

            if (list.size() == 1) {
                printer.append(" ").append(list.get(0)).append(" ");
            }
            else {
                printer.down().nextLine();
                for (final SimpleRecord<?> record : list)
                    printer.append(record).append(",").nextLine();
                printer.remove().up().nextLine();
            }
            printer.append("],").nextLine();
        }

        for (final Signature signature : children.keySet()) {
            final List<ComplexRecord> list = children.get(signature);
            printer.append("[").append(signature).append("]: [ ");

            for (int i = 0; i < list.size(); i++)
                printer.append(list.get(i)).append(", ");

            printer.remove().append(" ],").nextLine();
        }

        printer.remove().up().nextLine().append("}");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    /**
     * This method doesn't produce such fancy output as the printTo method, but it consistently sorts the keys and values so it's ideal for testing.
     */
    public String toComparableString() {
        final var sb = new StringBuilder();
        sb.append("{\n");

        for (final Signature signature : values.keySet()) {
            final String content = values.get(signature).stream()
                .map(SimpleRecord::toString)
                .sorted()
                .collect(Collectors.joining(", "));

            sb.append(signature).append(": [ ").append(content).append(" ],\n");
        }

        for (final Signature signature : children.keySet()) {
            final String content = children.get(signature).stream()
                .map(ComplexRecord::toComparableString)
                .sorted()
                .collect(Collectors.joining(", "));

            sb.append(signature).append(": [ ").append(content).append(" ],\n");
        }

        sb.append("}");

        return sb.toString();
    }
}
