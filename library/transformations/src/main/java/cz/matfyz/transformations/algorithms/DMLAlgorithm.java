package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.StaticName;
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

    private Mapping mapping;
    private InstanceCategory instance;
    private AbstractDMLWrapper wrapper;
    private Map<DynamicName, DynamicNameReplacement> replacedNames;

    public void input(Mapping mapping, InstanceCategory instance, AbstractDMLWrapper wrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.wrapper = wrapper;
        this.replacedNames = mapping.accessPath().copyWithoutDynamicNames().replacedNames();
    }

    public List<AbstractStatement> algorithm() {
        final InstanceObject instanceObject = instance.getObject(mapping.rootObject());
        final Set<DomainRow> domainRows = instanceObject.allRowsToSet();
        final Deque<DMLStackTriple> masterStack = new ArrayDeque<>();
        final List<AbstractStatement> output = new ArrayList<>();

        for (final DomainRow row : domainRows) {
            masterStack.push(new DMLStackTriple(row, AbstractDDLWrapper.EMPTY_NAME, mapping.accessPath()));
            output.add(buildStatement(masterStack));
        }

        return output;
    }

    private AbstractStatement buildStatement(Deque<DMLStackTriple> masterStack) {
        wrapper.clear();
        wrapper.setKindName(mapping.kindName());

        while (!masterStack.isEmpty()) {
            final DMLStackTriple triple = masterStack.pop();
            final List<NameValuePair> pairs = collectNameValuePairs(triple.complexProperty, triple.row);

            for (final var pair : pairs) {
                final String nameFromRoot = DMLAlgorithm.concatenatePaths(triple.name, pair.name);

                if (pair.isSimple)
                    wrapper.append(nameFromRoot, pair.simpleValue);
                else
                    masterStack.push(new DMLStackTriple(pair.complexValue, nameFromRoot, pair.property));
            }
        }

        return wrapper.createDMLStatement();
    }

    private List<NameValuePair> collectNameValuePairs(ComplexProperty path, DomainRow row) {
        return collectNameValuePairs(path, row, AbstractDDLWrapper.EMPTY_NAME);
    }

    private List<NameValuePair> collectNameValuePairs(ComplexProperty path, DomainRow row, String prefix) {
        final List<NameValuePair> output = new ArrayList<>();

        for (final AccessPath subpath : path.subpaths()) {
            if (subpath instanceof ComplexProperty complexSubpath && complexSubpath.isAuxiliary()) {
                // Auxiliary properties can't have dynamic names.
                final var staticName = (StaticName) complexSubpath.name();
                final String newPrefix = DMLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                output.addAll(collectNameValuePairs(complexSubpath, row, newPrefix));
                continue;
            }

            final var schemaPath = mapping.category().getPath(subpath.signature());

            if (subpath.name() instanceof final DynamicName dynamicName) {
                final var replacement = replacedNames.get(dynamicName);
                final var namePath = mapping.category().getPath(replacement.valueToName());

                for (final DomainRow objectRow : row.traverseThrough(schemaPath)) {
                    final var suffix = DDLAlgorithm.getDynamicNameValue(dynamicName, namePath, objectRow);
                    final String name = DMLAlgorithm.concatenatePaths(prefix, suffix);
                    output.add(createNameValuePair(subpath, objectRow, name));
                }
                continue;
            }

            // Now we know it's a normal property with a static name. It might be an array tho.
            final var staticName = (StaticName) subpath.name();

            int index = 0;
            for (final DomainRow objectRow : row.traverseThrough(schemaPath)) {
                final var suffix = staticName.getStringName() + (schemaPath.isArray() ? "[" + index + "]" : "");
                final String name = DMLAlgorithm.concatenatePaths(prefix, suffix);
                output.add(createNameValuePair(subpath, objectRow, name));
                index++;
            }

            // If it's an array but there aren't any items in it, we return a simple pair with 'null' value.
            if (schemaPath.isArray() && index == 0) {
                final String name = DMLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                output.add(new NameValuePair(name, null));
            }

            // Pro cassandru se nyní nerozlišuje mezi množinou (array bez duplicit) a polem (array).
            // Potom se to ale vyřeší.
        }

        return output;
    }

    private static String concatenatePaths(String path1, String path2) {
        return path1 + (path1.equals(AbstractDDLWrapper.EMPTY_NAME) ? "" : AbstractDDLWrapper.PATH_SEPARATOR) + path2;
    }

    private NameValuePair createNameValuePair(AccessPath objectPath, DomainRow objectRow, String name) {
        return objectPath instanceof final ComplexProperty complexPath
            ? new NameValuePair(name, objectRow, complexPath)
            : new NameValuePair(name, objectRow.getValue(Signature.createEmpty()));
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
