package cz.matfyz.core.record;

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

    public final Map<Signature, List<ComplexRecord>> children = new TreeMap<>();
    public final Map<Signature, List<SimpleRecord<?>>> values = new TreeMap<>();

    public ComplexRecord() {
        super();
    }

    public @Nullable List<ComplexRecord> getComplexRecords(Signature signature) {
        return children.get(signature);
    }

    public @Nullable List<SimpleRecord<?>> getSimpleRecords(Signature signature) {
        return values.get(signature);
    }

    public List<SimpleRecord<?>> findSimpleRecords(Signature signature) {
        final var directSimpleRecord = getSimpleRecords(signature);
        if (directSimpleRecord != null)
            return directSimpleRecord;

        final var auxiliaryChildren = children.get(Signature.createEmpty());
        if (auxiliaryChildren != null && auxiliaryChildren.size() == 1)
            return auxiliaryChildren.get(0).findSimpleRecords(signature);

        // There is no hope to find the simple record in the children because that would require at least two-part signature (one base to find the child and one to find the record in it).
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

            return childRecords.get(0).findSimpleRecords(signature.traverseAlong(currentPath));
        }

        return null;
    }

    public ComplexRecord addComplexRecord(Signature signature) {
        final ComplexRecord complexRecord = new ComplexRecord();
        children.computeIfAbsent(signature, x -> new ArrayList<>()).add(complexRecord);

        return complexRecord;
    }

    public <T> SimpleRecord<T> addSimpleRecord(Signature signature, T value) {
        final var simpleRecord = new SimpleRecord<>(value);
        values.computeIfAbsent(signature, x -> new ArrayList<>()).add(simpleRecord);

        return simpleRecord;
    }

    public ComplexRecord addDynamicReplacer(Signature signature, Signature nameSignature, String name) {
        final ComplexRecord dynamicWrapper = new ComplexRecord();
        children.computeIfAbsent(signature, x -> new ArrayList<>()).add(dynamicWrapper);

        dynamicWrapper.addSimpleRecord(nameSignature, name);

        return dynamicWrapper;
    }

    @Override public void printTo(Printer printer) {
        printer.append("{").down().nextLine();

        for (final Signature signature : values.keySet()) {
            final List<SimpleRecord<?>> list = values.get(signature);
            printer.append("(").append(signature.toString()).append("): [");

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
            printer.append("(").append(signature).append("): [ ");

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
        final StringBuilder builder = new StringBuilder();
        builder.append("{\n");

        for (final Signature signature : values.keySet()) {
            final String content = values.get(signature).stream()
                .map(SimpleRecord::toString)
                .sorted()
                .collect(Collectors.joining(", "));

            builder.append(signature.toString()).append(": [ ").append(content).append(" ],\n");
        }

        for (final Signature signature : children.keySet()) {
            final String content = children.get(signature).stream()
                .map(ComplexRecord::toComparableString)
                .sorted()
                .collect(Collectors.joining(", "));

            builder.append(signature).append(": [ ").append(content).append(" ],\n");
        }

        builder.append("}");

        return builder.toString();
    }
}
