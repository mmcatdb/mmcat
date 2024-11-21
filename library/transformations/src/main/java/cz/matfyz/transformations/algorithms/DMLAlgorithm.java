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
import cz.matfyz.transformations.exception.InvalidStateException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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

    public void input(Mapping mapping, InstanceCategory instance, AbstractDMLWrapper wrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.wrapper = wrapper;
    }

    public List<AbstractStatement> algorithm() {
        final InstanceObject instanceObject = instance.getObject(mapping.rootObject());
        final Set<DomainRow> domainRows = fetchSuperIds(instanceObject);
        final Deque<DMLStackTriple> masterStack = new ArrayDeque<>();
        final List<AbstractStatement> output = new ArrayList<>();

        for (final DomainRow row : domainRows) {
            masterStack.push(new DMLStackTriple(row, AbstractDDLWrapper.EMPTY_NAME, mapping.accessPath()));
            output.add(buildStatement(masterStack));
        }

        return output;
    }

    private Set<DomainRow> fetchSuperIds(InstanceObject object) {
        return object.allRowsToSet();
    }

    private AbstractStatement buildStatement(Deque<DMLStackTriple> masterStack) {
        wrapper.clear();
        wrapper.setKindName(mapping.kindName());

        while (!masterStack.isEmpty()) {
            final DMLStackTriple triple = masterStack.pop();
            final List<NameValuePair> pairs = collectNameValuePairs(triple.complexProperty, triple.row);

            for (final var pair : pairs) {
                String newName = DDLAlgorithm.concatenatePaths(triple.name, pair.name);

                if (pair.isSimple)
                    wrapper.append(newName, pair.simpleValue);
                else
                    masterStack.push(new DMLStackTriple(pair.complexValue, newName, pair.subpath));
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
                if (complexSubpath.name() instanceof StaticName staticName) {
                    final String newPrefix = DDLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                    output.addAll(collectNameValuePairs(complexSubpath, row, newPrefix));
                }
            }
            else {
                // Get all mapping rows that have signature of this subpath and originate in given row.
                if (subpath.signature().isEmpty()) {
                    // Self-identifier.
                    if (!(subpath.name() instanceof StaticName staticName))
                        continue; // This should not happen.

                    final String name = DDLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                    output.add(new NameValuePair(name, row.getValue(Signature.createEmpty())));

                    continue;
                }

                final var schemaPath = mapping.category().getPath(subpath.signature());
                final boolean isObjectWithDynamicKeys = subpath instanceof ComplexProperty complexSubpath && complexSubpath.hasDynamicKeys();
                final boolean showIndex = schemaPath.isArray() && !isObjectWithDynamicKeys;

                int index = 0;
                for (final DomainRow objectRow : row.traverseThrough(schemaPath)) {
                    output.add(getNameValuePair(subpath, row, objectRow, prefix, index, showIndex));
                    index++;
                }

                // If it's an array but there aren't any items in it, we return a simple pair with 'null' value.
                if (index == 0 && showIndex && subpath.name() instanceof StaticName staticName) {
                    final String name = DDLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                    output.add(new NameValuePair(name, null));
                }

                // Pro cassandru se nyní nerozlišuje mezi množinou (array bez duplicit) a polem (array).
                // Potom se to ale vyřeší.
            }
        }

        return output;
    }

    private NameValuePair getNameValuePair(AccessPath objectPath, DomainRow parentRow, DomainRow objectRow, String prefix, int index, boolean showIndex) {
        final String name = getStringName(objectPath, parentRow) + (showIndex ? "[" + index + "]" : "");
        final String fullName = DDLAlgorithm.concatenatePaths(prefix, name);

        if (objectPath instanceof ComplexProperty complexPath) {
            return new NameValuePair(fullName, objectRow, complexPath);
        }

        final String value = objectRow.getValue(Signature.createEmpty());
        return new NameValuePair(fullName, value);
    }

    private String getStringName(AccessPath objectPath, DomainRow parentRow) {
        if (objectPath.name() instanceof StaticName staticName)
            return staticName.getStringName();

        final var dynamicName = (DynamicName) objectPath.name();
        // If the name is dynamic, we have to find its string value.
        final var namePath = mapping.category().getPath(dynamicName.signature());
        final var nameRowSet = parentRow.traverseThrough(namePath);

        if (nameRowSet != null && !nameRowSet.isEmpty())
            return nameRowSet.iterator().next().getValue(Signature.createEmpty());

        throw InvalidStateException.dynamicNameNotFound(dynamicName);
    }

    private record NameValuePair(
        String name,
        @Nullable String simpleValue,
        @Nullable DomainRow complexValue,
        @Nullable ComplexProperty subpath,
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
