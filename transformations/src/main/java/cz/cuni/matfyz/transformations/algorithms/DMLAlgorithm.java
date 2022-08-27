package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.statements.DMLStatement;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author jachymb.bartik
 * @implNote A custom ordering of the elements of the arrays isn't supported in the current iteration of the framework.
 */
public class DMLAlgorithm {

    private Mapping mapping;
    private InstanceCategory category;
    private AbstractPushWrapper wrapper;

    public void input(Mapping mapping, InstanceCategory instance, AbstractPushWrapper wrapper) {
        this.mapping = mapping;
        this.category = instance;
        this.wrapper = wrapper;
    }
    
    public List<DMLStatement> algorithm() {
        return mapping.hasRootMorphism()
            ? processWithMorphism(mapping.rootMorphism()) // K with root morphism
            : processWithObject(mapping.rootObject()); // K with root object
    }

    private List<DMLStatement> processWithObject(SchemaObject object) {
        InstanceObject instanceObject = category.getObject(object);
        Set<DomainRow> domainRows = fetchSuperIds(instanceObject);
        Deque<DMLStackTriple> masterStack = new LinkedList<>();
        List<DMLStatement> output = new ArrayList<>();

        for (DomainRow domainRow : domainRows) {
            masterStack.push(new DMLStackTriple(domainRow, DDLAlgorithm.EMPTY_NAME, mapping.accessPath()));
            output.add(buildStatement(masterStack));
        }

        return output;
    }

    private List<DMLStatement> processWithMorphism(SchemaMorphism morphism) {
        InstanceMorphism instanceMorphism = category.getMorphism(morphism);
        Set<MappingRow> mappingRows = fetchRelations(instanceMorphism);
        AccessPath codomainPath = mapping.accessPath().getSubpathBySignature(morphism.signature());
        Deque<DMLStackTriple> masterStack = new LinkedList<>();
        List<DMLStatement> output = new ArrayList<>();

        if (codomainPath instanceof ComplexProperty complexPath) {
            for (MappingRow mappingRow : mappingRows) {
                masterStack.push(new DMLStackTriple(mappingRow.domainRow(), DDLAlgorithm.EMPTY_NAME, mapping.accessPath().minusSubpath(codomainPath)));
                masterStack.push(new DMLStackTriple(mappingRow.codomainRow(), DDLAlgorithm.EMPTY_NAME, complexPath));
                output.add(buildStatement(masterStack));
            }

            return output;
        }

        throw new UnsupportedOperationException("Process with morphism");
    }

    private Set<DomainRow> fetchSuperIds(InstanceObject object) {
        return object.allRows();
    }

    private Set<MappingRow> fetchRelations(InstanceMorphism morphism) {
        return morphism.allMappings();
    }

    private DMLStatement buildStatement(Deque<DMLStackTriple> masterStack) {
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
        return collectNameValuePairs(path, row, DDLAlgorithm.EMPTY_NAME);
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
                InstanceMorphism morphism = category.getMorphism(subpath.signature());
                boolean isObjectWithDynamicKeys = subpath instanceof ComplexProperty complexSubpath && complexSubpath.hasDynamicKeys();
                boolean showIndex = morphism.schemaMorphism().isArray() && !isObjectWithDynamicKeys;
                int index = 0;

                for (DomainRow objectRow : row.traverseThrough(morphism)) {
                    output.add(getNameValuePair(subpath, row, objectRow, prefix, index, showIndex));
                    index++;
                }

                // If it's aray but there aren't any items in it, we return a simple pair with 'null' value.
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

        if (objectPath instanceof SimpleProperty) {
            String value = objectRow.getValue(Signature.createEmpty());

            return new NameValuePair(fullName, value);
        }
        else if (objectPath instanceof ComplexProperty complexPath) {
            return new NameValuePair(fullName, objectRow, complexPath);
        }

        throw new UnsupportedOperationException("Get name value pair");
    }

    private String getStringName(AccessPath objectPath, DomainRow parentRow) {
        if (objectPath.name() instanceof StaticName staticName)
            return staticName.getStringName();

        var dynamicName = (DynamicName) objectPath.name();
        // If the name is dynamic, we have to find its string value.
        InstanceMorphism nameMorphism = category.getMorphism(dynamicName.signature());
        var nameRowSet = parentRow.traverseThrough(nameMorphism);

        if (nameRowSet != null && !nameRowSet.isEmpty())
            return nameRowSet.iterator().next().getValue(Signature.createEmpty());

        throw new UnsupportedOperationException("Dynamic name value not found.");
    }

    private class NameValuePair {
        public final String name;
        public final String simpleValue;
        public final DomainRow complexValue;
        public final ComplexProperty subpath;
        public final boolean isSimple;

        public NameValuePair(String name, String simpleValue) {
            this.name = name;
            this.simpleValue = simpleValue;
            this.complexValue = null;
            this.subpath = null;
            this.isSimple = true;
        }

        public NameValuePair(String name, DomainRow complexValue, ComplexProperty subpath) {
            this.name = name;
            this.simpleValue = null;
            this.complexValue = complexValue;
            this.subpath = subpath;
            this.isSimple = false;
        }
    }

}
