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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @implNote A custom ordering of the elements of the arrays isn't supported in the current iteration of the framework.
 */
public class DMLAlgorithm {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DMLAlgorithm.class);

    private Mapping mapping;
    private InstanceCategory category;
    private AbstractDMLWrapper wrapper;

    public void input(Mapping mapping, InstanceCategory instance, AbstractDMLWrapper wrapper) {
        this.mapping = mapping;
        this.category = instance;
        this.wrapper = wrapper;
    }

    public List<AbstractStatement> algorithm() {
        InstanceObject instanceObject = category.getObject(mapping.rootObject());
        Set<DomainRow> domainRows = fetchSuperIds(instanceObject);
        Deque<DMLStackTriple> masterStack = new ArrayDeque<>();
        List<AbstractStatement> output = new ArrayList<>();

        for (DomainRow domainRow : domainRows) {
            masterStack.push(new DMLStackTriple(domainRow, AbstractDDLWrapper.EMPTY_NAME, mapping.accessPath()));
            output.add(buildStatement(masterStack));
        }

        return output;
    }

    private Set<DomainRow> fetchSuperIds(InstanceObject object) {
        return object.allRowsToSet();
    }

    /*
    private Set<MappingRow> fetchRelations(InstanceMorphism morphism) {
        return morphism.allMappings();
    }
    */

    private AbstractStatement buildStatement(Deque<DMLStackTriple> masterStack) {
        wrapper.clear();
        wrapper.setKindName(mapping.kindName());

        while (!masterStack.isEmpty()) {
            DMLStackTriple triple = masterStack.pop();
            List<NameValuePair> pairs = collectNameValuePairs(triple.t, triple.pid);

            for (var pair : pairs) {
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
        List<NameValuePair> output = new ArrayList<>();

        for (AccessPath subpath : path.subpaths()) {
            if (subpath instanceof ComplexProperty complexSubpath && complexSubpath.isAuxiliary()) {
                if (complexSubpath.name() instanceof StaticName staticName) {
                    String newPrefix = DDLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                    output.addAll(collectNameValuePairs(complexSubpath, row, newPrefix));
                }
            }
            else {
                // Get all mapping rows that have signature of this subpath and originate in given row.
                if (subpath.signature().isEmpty()) {
                    // Self-identifier.
                    if (!(subpath.name() instanceof StaticName staticName))
                        continue; // This should not happen.

                    String name = DDLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                    output.add(new NameValuePair(name, row.getValue(Signature.createEmpty())));

                    continue;
                }

                final var instancePath = category.getPath(subpath.signature());
                final boolean isObjectWithDynamicKeys = subpath instanceof ComplexProperty complexSubpath && complexSubpath.hasDynamicKeys();
                final boolean showIndex = instancePath.isArray() && !isObjectWithDynamicKeys;

                int index = 0;
                for (DomainRow objectRow : row.traverseThrough(instancePath)) {
                    output.add(getNameValuePair(subpath, row, objectRow, prefix, index, showIndex));
                    index++;
                }

                // If it's an array but there aren't any items in it, we return a simple pair with 'null' value.
                if (index == 0 && showIndex && subpath.name() instanceof StaticName staticName) {
                    String name = DDLAlgorithm.concatenatePaths(prefix, staticName.getStringName());
                    output.add(new NameValuePair(name, null));
                }

                // Pro cassandru se nyní nerozlišuje mezi množinou (array bez duplicit) a polem (array).
                // Potom se to ale vyřeší.
            }
        }

        return output;
    }

    private NameValuePair getNameValuePair(AccessPath objectPath, DomainRow parentRow, DomainRow objectRow, String prefix, int index, boolean showIndex) {
        String name = getStringName(objectPath, parentRow) + (showIndex ? "[" + index + "]" : "");
        String fullName = DDLAlgorithm.concatenatePaths(prefix, name);

        if (objectPath instanceof ComplexProperty complexPath) {
            return new NameValuePair(fullName, objectRow, complexPath);
        }

        String value = objectRow.getValue(Signature.createEmpty());
        return new NameValuePair(fullName, value);
    }

    private String getStringName(AccessPath objectPath, DomainRow parentRow) {
        if (objectPath.name() instanceof StaticName staticName)
            return staticName.getStringName();

        var dynamicName = (DynamicName) objectPath.name();
        // If the name is dynamic, we have to find its string value.
        final var namePath = category.getPath(dynamicName.signature());
        var nameRowSet = parentRow.traverseThrough(namePath);

        if (nameRowSet != null && !nameRowSet.isEmpty())
            return nameRowSet.iterator().next().getValue(Signature.createEmpty());

        throw InvalidStateException.dynamicNameNotFound(dynamicName);
    }

    private class NameValuePair {
        public final String name;
        public final String simpleValue;
        public final DomainRow complexValue;
        public final ComplexProperty subpath;
        public final boolean isSimple;

        NameValuePair(String name, String simpleValue) {
            this.name = name;
            this.simpleValue = simpleValue;
            this.complexValue = null;
            this.subpath = null;
            this.isSimple = true;
        }

        NameValuePair(String name, DomainRow complexValue, ComplexProperty subpath) {
            this.name = name;
            this.simpleValue = null;
            this.complexValue = complexValue;
            this.subpath = subpath;
            this.isSimple = false;
        }

        @Override public String toString() {
            return isSimple
                ? "[simple] " + name + " \"" + simpleValue + "\" "
                : "[complex] " + complexValue;
        }
    }

}
