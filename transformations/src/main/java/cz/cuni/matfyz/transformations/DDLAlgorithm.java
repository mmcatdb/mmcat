package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.statements.DDLStatement;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class DDLAlgorithm
{
    private SchemaCategory schema; // TODO
    private InstanceFunctor instanceFunctor;
    private String name; // TODO
    private ComplexProperty rootAccessPath;
    private AbstractDDLWrapper wrapper;
    
    public void input(SchemaCategory schema, InstanceCategory instance, String name, ComplexProperty rootAccessPath, AbstractDDLWrapper wrapper)
    {
        this.schema = schema;
        instanceFunctor = new InstanceFunctor(instance, schema);
        this.name = name;
        this.rootAccessPath = rootAccessPath;
        this.wrapper = wrapper;
    }
    
    public DDLStatement algorithm()
    {
        wrapper.setKindName(name);
        
        if (!wrapper.isSchemaLess())
        {
            Stack<StackPair> M = new Stack<>();
            M.add(new StackPair(Set.of(StaticName.Anonymous().toString()), rootAccessPath));

            while (!M.isEmpty())
                processTopOfStack(M);
        }
        
        return wrapper.createDDLStatement();
    }
    
    private void processTopOfStack(Stack<StackPair> M)
    {
        StackPair pair = M.pop();
        AccessPath path = pair.accessPath;
        
        Set<String> Nt = determinePropertyName(path);
        Set<String> N = concatenate(pair.names, Nt);
        
        if (path instanceof SimpleProperty simpleProperty)
            processPath(simpleProperty, N);
        else if (path instanceof ComplexProperty complexProperty)
            processPath(complexProperty, N);
    }
    
    private Set<String> determinePropertyName(AccessPath path)
    {
        if (path.name() instanceof StaticName staticName)
            return Set.of(staticName.getStringName());
        
        var dynamicName = (DynamicName) path.name();
            
        SchemaObject schemaObject = schema.morphisms().get(dynamicName.signature()).cod();
        InstanceObject instanceObject = instanceFunctor.object(schemaObject);
        
        var output = new TreeSet<String>();
        for (ActiveDomainRow row : instanceObject.activeDomain().get(new Id(Signature.Empty())).values())
            output.add(row.getValue(Signature.Empty()));
        
        return output;
    }
    
    private Set<String> concatenate(Set<String> names1, Set<String> names2)
    {
        var output = new TreeSet<String>();
        for (String name1 : names1)
            for (String name2 : names2)
                output.add(name1 + "/" + name2);
        
        return output;
    }
    
    private void processPath(SimpleProperty property, Set<String> names)
    {
        var morphism = schema.morphisms().get(property.value().signature());
        
        if (isArray(morphism))
            wrapper.addSimpleArrayProperty(names, isOptional(morphism));
        else
            wrapper.addSimpleProperty(names, isOptional(morphism));
    }
    
    private void processPath(ComplexProperty property, Set<String> names)
    {
        var morphism = schema.morphisms().get(property.signature());
        
        if (isArray(morphism))
            wrapper.addComplexArrayProperty(names, isOptional(morphism));
        else
            wrapper.addComplexProperty(names, isOptional(morphism));
    }
    
    private static boolean isOptional(SchemaMorphism morphism)
    {
        return morphism.min() == SchemaMorphism.Min.ZERO;
    }
    
    private static boolean isArray(SchemaMorphism morphism)
    {
        return morphism.max() == SchemaMorphism.Max.STAR;
    }
}
