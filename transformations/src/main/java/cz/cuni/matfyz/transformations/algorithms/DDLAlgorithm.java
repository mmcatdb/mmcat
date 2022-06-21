package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.statements.DDLStatement;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class DDLAlgorithm
{
    private Mapping mapping;
    private InstanceCategory instance;
    private AbstractDDLWrapper wrapper;
    
    public void input(Mapping mapping, InstanceCategory instance, AbstractDDLWrapper wrapper)
    {
        this.mapping = mapping;
        this.instance = instance;
        this.wrapper = wrapper;
    }
    
    public DDLStatement algorithm()
    {
        wrapper.setKindName(mapping.kindName());
        
        if (!wrapper.isSchemaLess())
        {
            Stack<StackPair> M = new Stack<>();
            addSubpathsToStack(M, mapping.accessPath(), Set.of(StaticName.Anonymous().getStringName()));

            while (!M.isEmpty())
                processTopOfStack(M);
        }
        
        return wrapper.createDDLStatement();
    }

    private void addSubpathsToStack(Stack<StackPair> M, ComplexProperty path, Set<String> names)
    {
        for (AccessPath subpath : path.subpaths())
            M.add(new StackPair(names, subpath));
    }
    
    private void processTopOfStack(Stack<StackPair> M)
    {
        StackPair pair = M.pop();
        AccessPath path = pair.accessPath;
        
        Set<String> Nt = determinePropertyName(path);
        Set<String> N = concatenate(pair.names, Nt);
        
        if (path instanceof SimpleProperty simpleProperty)
        {
            processPath(simpleProperty, N);
        }
        else if (path instanceof ComplexProperty complexProperty)
        {
            if (!complexProperty.isAuxiliary())
                processPath(complexProperty, N);

            addSubpathsToStack(M, complexProperty, N);
        }
    }
    
    private Set<String> determinePropertyName(AccessPath path)
    {
        if (path.name() instanceof StaticName staticName)
            return Set.of(staticName.getStringName());
        
        var dynamicName = (DynamicName) path.name();
            
        SchemaObject schemaObject = instance.getMorphism(dynamicName.signature()).schemaMorphism().cod();
        InstanceObject instanceObject = instance.getObject(schemaObject);
        
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
        var morphism = instance.getMorphism(property.value().signature()).schemaMorphism();
        
        if (morphism.isArray())
            wrapper.addSimpleArrayProperty(names, isRequired(morphism));
        else
            wrapper.addSimpleProperty(names, isRequired(morphism));
    }
    
    private void processPath(ComplexProperty property, Set<String> names)
    {
        var morphism = instance.getMorphism(property.signature()).schemaMorphism();
        
        if (morphism.isArray())
            wrapper.addComplexArrayProperty(names, isRequired(morphism));
        else
            wrapper.addComplexProperty(names, isRequired(morphism));
    }
    
    private static boolean isRequired(SchemaMorphism morphism)
    {
        return morphism.min() != SchemaMorphism.Min.ZERO;
    }
}
