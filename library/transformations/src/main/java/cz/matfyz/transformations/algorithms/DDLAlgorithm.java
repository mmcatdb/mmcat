package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper.PathSegment;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper.PropertyPath;
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

        final PropertyPath path = element.path().add(createPathSegment(property));

        if (property instanceof final ComplexProperty complexProperty)
            addSubpathsToStack(masterStack, complexProperty, path);

        final boolean isComplex = property instanceof ComplexProperty;
        final boolean isRequired = isRequired(property, schemaPath);
        wrapper.addProperty(path, isComplex, isRequired);
    }

    private PathSegment createPathSegment(AccessPath property) {
        // There are two ways how to represent arrays:
        // - A set (index isn't mapped):
        // order: {
        //     tags: -1.3
        // }
        //
        // - An array (index is mapped):
        // order: {
        //     tags: {
        //         <index: -1.2>: -1.3
        //     }
        // }
        //
        // The second one also allows multi-dimensional arrays:
        // data: {
        //     values: {
        //         <index: -1.2>: -1.3 {
        //             <index: -5.6>: -5.7
        //         }
        //     }
        // }
        //
        // The first one is just a 'shorthand' for single-dimensional arrays - i.e., "tags[]".
        // The second one is the same ("tags[]"), but the multi-dimensional variant is more complex ("values[][]").

        if (property.name() instanceof final StringName stringName)
            return PathSegment.scalar(stringName.value);

        if (!(property.name() instanceof DynamicName dynamicName))
            throw InvalidStateException.nameIsTyped(property.name());

        // FIXME build only
        // if (dynamicName.type.equals(DynamicName.INDEX))
        //     return PathSegment.array();

        return PathSegment.map(getPropertyNames(dynamicName));
    }

    private Set<String> getPropertyNames(DynamicName dynamicName) {
        final var replacement = replacedNames.get(dynamicName);

        final var prefixPath = mapping.category().getPath(replacement.prefix());
        final var mapObjex = instance.getObjex(prefixPath.to());

        final var names = new TreeSet<String>();

        for (final var row : mapObjex.allRowsToSet()) {
            // It has to be a scalar value because each map entry has to have a single name.
            final var name = row.tryFindScalarValue(replacement.name());
            if (name != null)
                names.add(name);
        }

        return names;
    }

    private static boolean isRequired(AccessPath property, SchemaPath schemaPath) {
        return property.isRequired() || schemaPath.min() != Min.ZERO;
    }

}
