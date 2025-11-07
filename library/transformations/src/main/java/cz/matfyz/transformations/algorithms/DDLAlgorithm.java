package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper.PathSegment;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper.PropertyPath;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaMorphism.Min;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.TreeSet;

public class DDLAlgorithm {

    public static AbstractStatement run(Mapping mapping, InstanceCategory instance, AbstractDDLWrapper wrapper) {
        return new DDLAlgorithm(mapping, instance, wrapper).run();
    }

    private final Mapping mapping;
    private final InstanceCategory instance;
    private final AbstractDDLWrapper wrapper;

    private DDLAlgorithm(Mapping mapping, InstanceCategory instance, AbstractDDLWrapper wrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.wrapper = wrapper;
    }

    private record StackJob(ComplexProperty path, PropertyPath prefix) {}

    private final Deque<StackJob> stack = new ArrayDeque<>();

    private AbstractStatement run() {
        stack.clear();

        wrapper.clear();
        wrapper.setKindName(mapping.kindName());

        if (!wrapper.isSchemaless()) {
            stack.push(new StackJob(mapping.accessPath(), PropertyPath.empty()));

            while (!stack.isEmpty()) {
                final StackJob job = stack.pop();
                processComplexProperty(job.path, job.prefix);
            }
        }

        return wrapper.createDDLStatement();
    }

    private void processComplexProperty(ComplexProperty path, PropertyPath prefix) {
        for (final AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof DynamicName) {
                processMap((ComplexProperty) subpath, prefix);
            }
            else {
                final var name = prefix.add(PathSegment.scalar(((StringName) subpath.name()).value));
                processSubpath(subpath, name);
            }
        }
    }

    private void processMap(ComplexProperty map, PropertyPath prefix) {
        final var valueProperty = map.getTypedSubpath(TypedName.VALUE);

        final var name = prefix.add(PathSegment.map(getDynamicPropertyNames(map)));
        processSubpath(valueProperty, name);
    }

    private Set<String> getDynamicPropertyNames(ComplexProperty map) {
        final var schemaPath = mapping.category().getPath(map.signature());
        final var mapObjex = instance.getObjex(schemaPath.to());
        final var names = new TreeSet<String>();
        final var keyProperty = map.getTypedSubpath(TypedName.KEY);

        for (final var row : mapObjex.allRows()) {
            // It has to be a scalar value because each map entry has to have a single name.
            final var name = row.tryFindScalarValue(keyProperty.signature());
            if (name != null)
                names.add(name);
        }

        return names;
    }

    private void processSubpath(AccessPath subpath, PropertyPath prefix) {
        if (!(subpath instanceof final ComplexProperty complex)) {
            processSimpleSubpath((SimpleProperty) subpath, prefix);
            return;
        }

        if (!complex.signature().hasDual()) {
            // Not an array.
            addProperty(complex, prefix);
            return;
        }

        // It's an array. It still might have mapped indexes.
        if (!complex.getIndexSubpaths().isEmpty()) {
            processArray(complex, prefix);
            return;
        }

        // It's a complex array without mapped indexes.
        final var name = prefix.addArray(1);
        addProperty(subpath, name);
    }

    private void processSimpleSubpath(SimpleProperty subpath, PropertyPath prefix) {
        // Now we know it's a normal property with a string name.
        if (!subpath.signature().hasDual()) {
            // Not an array.
            addProperty(subpath, prefix);
            return;
        }

        // It's a simple array without mapped indexes.
        final var name = prefix.addArray(1);
        addProperty(subpath, name);
    }

    private void processArray(ComplexProperty array, PropertyPath prefix) {
        final var dimensions = array.getIndexSubpaths().size();
        final var name = prefix.addArray(dimensions);
        final var valueSubpath = array.getTypedSubpath(TypedName.VALUE);
        processSubpath(valueSubpath, name);
    }

    private void addProperty(AccessPath path, PropertyPath name) {
        wrapper.addProperty(name, path instanceof ComplexProperty, isRequired(path, name));

        if (path instanceof final ComplexProperty complex)
            stack.push(new StackJob(complex, name));
    }

    private boolean isRequired(AccessPath path, PropertyPath name) {
        if (path.isRequired())
            return true;

        final var last = name.segments().get(name.segments().size() - 1);
        if (last.isArray || last.isMap)
            return false;

        final var schemaPath = mapping.category().getPath(path.signature());
        return schemaPath.min() != Min.ZERO;
    }

}
