package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper.PathSegment;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper.PropertyPath;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.transformations.exception.InvalidStateException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DDLAlgorithm {

    public static AbstractStatement run(Mapping mapping, InstanceCategory instance, AbstractDDLWrapper wrapper) {
        return new DDLAlgorithm(mapping, instance, wrapper).run();
    }

    private final Mapping mapping;
    private final InstanceCategory instance;
    private final AbstractDDLWrapper wrapper;
    private final Map<DynamicName, DynamicNameReplacement> replacedNames;

    private DDLAlgorithm(Mapping mapping, InstanceCategory instance, AbstractDDLWrapper wrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.wrapper = wrapper;
        this.replacedNames = mapping.accessPath().copyWithoutDynamicNames().replacedNames();
    }

    private record StackElement(
        PropertyPath path,
        AccessPath property
    ) {}

    private AbstractStatement run() {
        wrapper.clear();
        wrapper.setKindName(mapping.kindName());

        if (!wrapper.isSchemaless()) {
            final Deque<StackElement> masterStack = new ArrayDeque<>();
            addSubpathsToStack(masterStack, mapping.accessPath(), PropertyPath.empty());

            while (!masterStack.isEmpty())
                processTopOfStack(masterStack);
        }

        return wrapper.createDDLStatement();
    }

    private void addSubpathsToStack(Deque<StackElement> masterStack, ComplexProperty property, PropertyPath path) {
        for (final AccessPath subpath : property.subpaths())
            masterStack.push(new StackElement(path, subpath));
    }

    private void processTopOfStack(Deque<StackElement> masterStack) {
        final StackElement element = masterStack.pop();
        final AccessPath property = element.property();
        final SchemaPath schemaPath = mapping.category().getPath(property.signature());

        final Set<String> names = getPropertyNames(property);
        final var isDynamic = property.name() instanceof DynamicName;
        final boolean isArray = schemaPath.isArray() && !isDynamic;
        final PropertyPath path = element.path().add(new PathSegment(names, isDynamic, isArray));

        if (property instanceof final ComplexProperty complexProperty)
            addSubpathsToStack(masterStack, complexProperty, path);

        final boolean isComplex = property instanceof ComplexProperty;
        final boolean isRequired = isRequired(property, schemaPath);
        wrapper.addProperty(path, isComplex, isRequired);

    }

    private Set<String> getPropertyNames(AccessPath property) {
        if (property.name() instanceof final StringName stringName)
            return Set.of(stringName.value);

        final var dynamicName = (DynamicName) property.name();
        final var replacement = replacedNames.get(dynamicName);
        final var namePath = mapping.category().getPath(replacement.valueToName());

        final var schemaObjex = mapping.category().getPath(property.signature()).to();
        final var objexRows = instance.getObjex(schemaObjex).allRowsToSet();
        final var names = new TreeSet<String>();

        objexRows.forEach(row -> names.add(getDynamicNameValue(dynamicName, namePath, row)));

        return names;
    }

    public static String getDynamicNameValue(DynamicName dynamicName, SchemaPath namePath, DomainRow objexRow) {
        final var nameRowSet = objexRow.traverseThrough(namePath);

        if (nameRowSet.isEmpty())
            throw InvalidStateException.dynamicNameNotFound(dynamicName);
        if (nameRowSet.size() > 1)
            throw InvalidStateException.dynamicNameNotUnique(dynamicName);

        return nameRowSet.iterator().next().getValue(Signature.createEmpty());
    }

    private static boolean isRequired(AccessPath property, SchemaPath schemaPath) {
        return property.isRequired() || schemaPath.min() != Min.ZERO;
    }

}
