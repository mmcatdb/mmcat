package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.schema.SchemaMorphism.Min;

import java.util.ArrayDeque;
import java.util.Deque;

public class DDLAlgorithm {

    private Mapping mapping;
    private SchemaCategory category;
    private AbstractDDLWrapper wrapper;

    public void input(Mapping mapping, SchemaCategory schema, AbstractDDLWrapper wrapper) {
        this.mapping = mapping;
        this.category = schema;
        this.wrapper = wrapper;
    }

    record StackElement(
        String path,
        AccessPath property
    ) {}

    public AbstractStatement algorithm() {
        wrapper.setKindName(mapping.kindName());

        if (!wrapper.isSchemaless()) {
            Deque<StackElement> masterStack = new ArrayDeque<>();
            addSubpathsToStack(masterStack, mapping.accessPath(), AbstractDDLWrapper.EMPTY_NAME);

            while (!masterStack.isEmpty())
                processTopOfStack(masterStack);
        }

        return wrapper.createDDLStatement();
    }

    private void addSubpathsToStack(Deque<StackElement> masterStack, ComplexProperty property, String path) {
        for (AccessPath subpath : property.subpaths())
            masterStack.push(new StackElement(path, subpath));
    }

    private void processTopOfStack(Deque<StackElement> masterStack) {
        final StackElement element = masterStack.pop();
        final AccessPath property = element.property();

        final String name = determinePropertyName(property);
        final String path = concatenatePaths(element.path(), name);

        if (property instanceof SimpleProperty simpleProperty) {
            processPath(simpleProperty, path);
        }
        else if (property instanceof ComplexProperty complexProperty) {
            if (!complexProperty.isAuxiliary())
                processPath(complexProperty, path);

            addSubpathsToStack(masterStack, complexProperty, path);
        }
    }

    private String determinePropertyName(AccessPath property) {
        if (property.name() instanceof StaticName staticName)
            return staticName.getStringName();

        return AbstractDDLWrapper.DYNAMIC_NAME;
    }

    public static String concatenatePaths(String path1, String path2) {
        return path1.equals(AbstractDDLWrapper.EMPTY_NAME)
            ? path2
            : path1 + AbstractDDLWrapper.PATH_SEPARATOR + path2;
    }

    private void processPath(SimpleProperty property, String path) {
        // If the signature is empty, it is a self-identifier. Then it has to have a static name.
        if (property.signature().isEmpty()) {
            wrapper.addSimpleProperty(path, true);
            return;
        }

        final SchemaPath schemaPath = category.getPath(property.signature());
        final boolean isRequired = isRequired(property, schemaPath);

        if (schemaPath.isArray() && property.name() instanceof StaticName)
            wrapper.addSimpleArrayProperty(path, isRequired);
        else
            wrapper.addSimpleProperty(path, isRequired);
    }

    private void processPath(ComplexProperty property, String path) {
        final SchemaPath schemaPath = category.getPath(property.signature());
        final boolean isRequired = isRequired(property, schemaPath);

        if (schemaPath.isArray() && !property.hasDynamicKeys())
            wrapper.addComplexArrayProperty(path, isRequired);
        else
            wrapper.addComplexProperty(path, isRequired);
    }

    private static boolean isRequired(AccessPath property, SchemaPath schemaPath) {
        return property.isRequired() || schemaPath.min() != Min.ZERO;
    }

}
