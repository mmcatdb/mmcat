package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObjex;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.transformations.exception.InvalidStateException;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @implNote A custom ordering of the elements of the arrays isn't supported in the current iteration of the framework.
 */
public class DMLAlgorithm {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DMLAlgorithm.class);

    public static List<AbstractStatement> run(Mapping mapping, InstanceCategory instance, AbstractDMLWrapper wrapper) {
        return new DMLAlgorithm(mapping, instance, wrapper).run();
    }

    private final Mapping mapping;
    private final InstanceCategory instance;
    private final AbstractDMLWrapper wrapper;
    private final Map<DynamicName, DynamicNameReplacement> replacedNames;

    private DMLAlgorithm(Mapping mapping, InstanceCategory instance, AbstractDMLWrapper wrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.wrapper = wrapper;
        this.replacedNames = mapping.accessPath().copyWithoutDynamicNames().replacedNames();
    }

    private List<AbstractStatement> run() {
        final InstanceObjex instanceObjex = instance.getObjex(mapping.rootObjex());
        final Set<DomainRow> domainRows = instanceObjex.allRowsToSet();
        final Deque<DMLStackTriple> stack = new ArrayDeque<>();
        final List<AbstractStatement> output = new ArrayList<>();

        for (final DomainRow row : domainRows) {
            stack.push(new DMLStackTriple(row, AbstractDDLWrapper.EMPTY_NAME, mapping.accessPath()));
            output.add(buildStatement(stack));
        }

        return output;
    }

    private AbstractStatement buildStatement(Deque<DMLStackTriple> stack) {
        wrapper.clear();
        wrapper.setKindName(mapping.kindName());

        while (!stack.isEmpty()) {
            final DMLStackTriple triple = stack.pop();
            final List<NameValuePair> pairs = collectNameValuePairs(triple.complexProperty, triple.row, AbstractDDLWrapper.EMPTY_NAME);

            for (final var pair : pairs) {
                final String nameFromRoot = concatenatePaths(triple.name, pair.name);

                if (pair.isSimple)
                    wrapper.append(nameFromRoot, pair.simpleValue);
                else
                    stack.push(new DMLStackTriple(pair.complexValue, nameFromRoot, pair.property));
            }
        }

        return wrapper.createDMLStatement();
    }

    private List<NameValuePair> collectNameValuePairs(ComplexProperty path, DomainRow row, String prefix) {
        final List<NameValuePair> output = new ArrayList<>();

        for (final AccessPath subpath : path.subpaths()) {
            if (subpath instanceof final ComplexProperty complexSubpath)
                addNameValuePairsForComplex(complexSubpath, row, prefix, output);
            else
                addNameValuePairsForSimple((SimpleProperty) subpath, row, prefix, output);
        }

        return output;
    }

    private void addNameValuePairsForComplex(ComplexProperty subpath, DomainRow row, String prefix, List<NameValuePair> output) {
        if (subpath.isAuxiliary()) {
            // Auxiliary properties can't have dynamic names.
            final var stringName = (StringName) subpath.name();
            final String newPrefix = concatenatePaths(prefix, stringName.value);
            output.addAll(collectNameValuePairs(subpath, row, newPrefix));
            return;
        }

        final var schemaPath = mapping.category().getPath(subpath.signature());

        if (subpath.name() instanceof final DynamicName dynamicName) {
            final var replacement = replacedNames.get(dynamicName);

            for (final DomainRow entryRow : row.traverseThrough(replacement.prefix())) {
                final var suffix = entryRow.tryFindScalarValue(replacement.name());
                if (suffix == null)
                    throw InvalidStateException.dynamicNameNotFound(dynamicName);

                final String name = concatenatePaths(prefix, suffix);

                final var valueRows = entryRow.traverseThrough(replacement.value().signature());
                assert valueRows.size() <= 1 : "Dynamic property with a static name must have at most one value.";

                if (valueRows.isEmpty())
                    output.add(new NameValuePair(name, null));
                else
                    output.add(new NameValuePair(name, valueRows.iterator().next(), subpath));
            }
            return;
        }

        // Now we know it's a normal property with a static name. It might be an array tho.
        final var stringName = (StringName) subpath.name();
        final String name = concatenatePaths(prefix, stringName.value);

        // It's a complex property, so the name can't be identified by a value. So we can safely collect child domain rows.

        if (!subpath.signature().hasDual()) {
            final var childRows = row.traverseThrough(subpath.signature());
            assert childRows.size() <= 1 : "Complex property with a static name must have at most one value.";

            if (childRows.isEmpty())
                output.add(new NameValuePair(name, null));
            else
                output.add(new NameValuePair(name, childRows.iterator().next(), subpath));

            return;
        }

        // It's an array.
        int index = 0;
        for (final DomainRow objexRow : row.traverseThrough(schemaPath)) {
            output.add(new NameValuePair(name + "[" + index + "]", objexRow, subpath));
            index++;
        }

        // If it's an array but there aren't any items in it, we return a simple pair with 'null' value.
        if (index == 0)
            output.add(new NameValuePair(name, null));
    }

    private void addNameValuePairsForSimple(SimpleProperty subpath, DomainRow row, String prefix, List<NameValuePair> output) {
        if (subpath.name() instanceof final DynamicName dynamicName) {
            final var replacement = replacedNames.get(dynamicName);

            for (final DomainRow entryRow : row.traverseThrough(replacement.prefix())) {
                final var suffix = entryRow.tryFindScalarValue(replacement.name());
                if (suffix == null)
                    throw InvalidStateException.dynamicNameNotFound(dynamicName);

                final String name = concatenatePaths(prefix, suffix);
                output.add(new NameValuePair(name, entryRow.tryFindScalarValue(replacement.value().signature())));
            }
            return;
        }

        // Now we know it's a normal property with a static name. It might be an array tho.
        final var stringName = (StringName) subpath.name();
        final String name = concatenatePaths(prefix, stringName.value);

        if (!subpath.signature().hasDual()) {
            final @Nullable String value = row.tryFindScalarValue(subpath.signature());
            output.add(new NameValuePair(name, value));
            return;
        }

        // It's an array.
        int index = 0;
        for (final String value : row.findArrayValues(subpath.signature())) {
            output.add(new NameValuePair(name + "[" + index + "]", value));
            index++;
        }

        // If it's an array but there aren't any items in it, we return a simple pair with 'null' value.
        if (index == 0)
            output.add(new NameValuePair(name, null));
    }

    private static String concatenatePaths(String path1, String path2) {
        return path1 + (path1.equals(AbstractDDLWrapper.EMPTY_NAME) ? "" : AbstractDDLWrapper.PATH_SEPARATOR) + path2;
    }

    private record NameValuePair(
        String name,
        @Nullable String simpleValue,
        @Nullable DomainRow complexValue,
        @Nullable ComplexProperty property,
        boolean isSimple
    ) {
        NameValuePair(String name, @Nullable String simpleValue) {
            this(name, simpleValue, null, null, true);
        }

        NameValuePair(String name, DomainRow complexValue, ComplexProperty subpath) {
            this(name, null, complexValue, subpath, false);
        }

        @Override public String toString() {
            return isSimple
                ? "[simple] " + name + " \"" + simpleValue + "\" "
                : "[complex] " + complexValue;
        }
    }

}
