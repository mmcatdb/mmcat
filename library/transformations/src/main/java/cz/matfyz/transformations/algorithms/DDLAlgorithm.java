package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.category.Morphism.Min;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategory.InstancePath;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author jachymb.bartik
 */
public class DDLAlgorithm {

    private Mapping mapping;
    private InstanceCategory category;
    private AbstractDDLWrapper wrapper;

    public void input(Mapping mapping, InstanceCategory instance, AbstractDDLWrapper wrapper) {
        this.mapping = mapping;
        this.category = instance;
        this.wrapper = wrapper;
    }

    record StackElement(
        Set<String> names,
        AccessPath accessPath
    ) {}

    public AbstractStatement algorithm() {
        wrapper.setKindName(mapping.kindName());

        if (!wrapper.isSchemaLess()) {
            Deque<StackElement> masterStack = new ArrayDeque<>();
            addSubpathsToStack(masterStack, mapping.accessPath(), Set.of(AbstractDDLWrapper.EMPTY_NAME));

            while (!masterStack.isEmpty())
                processTopOfStack(masterStack);
        }

        return wrapper.createDDLStatement();
    }

    private void addSubpathsToStack(Deque<StackElement> masterStack, ComplexProperty path, Set<String> names) {
        for (AccessPath subpath : path.subpaths())
            masterStack.push(new StackElement(names, subpath));
    }

    private void processTopOfStack(Deque<StackElement> masterStack) {
        StackElement element = masterStack.pop();
        AccessPath path = element.accessPath();

        Set<String> propertyName = determinePropertyName(path);
        Set<String> names = concatenate(element.names(), propertyName);

        if (path instanceof SimpleProperty simpleProperty) {
            processPath(simpleProperty, names);
        }
        else if (path instanceof ComplexProperty complexProperty) {
            if (!complexProperty.isAuxiliary())
                processPath(complexProperty, names);

            addSubpathsToStack(masterStack, complexProperty, names);
        }
    }

    private Set<String> determinePropertyName(AccessPath path) {
        if (path.name() instanceof StaticName staticName)
            return Set.of(staticName.getStringName());

        var dynamicName = (DynamicName) path.name();

        InstanceObject instanceObject = category.getMorphism(dynamicName.signature()).cod();

        var output = new TreeSet<String>();
        // The rows have to have only empty signature so we can just pull all rows.
        for (DomainRow row : instanceObject.allRowsToSet())
            output.add(row.getValue(Signature.createEmpty()));

        return output;
    }

    private Set<String> concatenate(Set<String> names1, Set<String> names2) {
        var output = new TreeSet<String>();
        for (String name1 : names1)
            for (String name2 : names2)
                output.add(concatenatePaths(name1, name2));

        return output;
    }

    public static String concatenatePaths(String path1, String path2) {
        return AbstractDDLWrapper.EMPTY_NAME.equals(path1)
            ? path2
            : path1 + AbstractDDLWrapper.PATH_SEPARATOR + path2;
    }

    private void processPath(SimpleProperty property, Set<String> names) {
        // If the signature is empty, it is a self-identifier. Then it has to have a static name.
        if (property.signature().isEmpty()) {
            wrapper.addSimpleProperty(names, true);
            return;
        }

        final var path = category.getPath(property.signature());
        final var isRequired = isRequired(property, path);

        if (path.isArray() && property.name() instanceof StaticName)
            wrapper.addSimpleArrayProperty(names, isRequired);
        else
            wrapper.addSimpleProperty(names, isRequired);
    }

    private void processPath(ComplexProperty property, Set<String> names) {
        final var path = category.getPath(property.signature());
        final var isRequired = isRequired(property, path);

        if (path.isArray() && !property.hasDynamicKeys())
            wrapper.addComplexArrayProperty(names, isRequired);
        else
            wrapper.addComplexProperty(names, isRequired);
    }

    private static boolean isRequired(AccessPath property, InstancePath path) {
        return property.isRequired() || path.min() != Min.ZERO;
    }
}
