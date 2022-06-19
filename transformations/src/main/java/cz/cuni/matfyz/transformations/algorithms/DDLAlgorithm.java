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
    private SchemaCategory schema; // TODO
    private InstanceCategory instance;
    private String rootName; // TODO
    private ComplexProperty rootAccessPath;
    private AbstractDDLWrapper wrapper;
    
    public void input(SchemaCategory schema, InstanceCategory instance, String rootName, ComplexProperty rootAccessPath, AbstractDDLWrapper wrapper)
    {
        this.schema = schema;
        this.instance = instance;
        this.rootName = rootName;
        this.rootAccessPath = rootAccessPath;
        this.wrapper = wrapper;
    }
    
    public DDLStatement algorithm()
    {
        wrapper.setKindName(rootName);
        
        if (!wrapper.isSchemaLess())
        {
            Stack<StackPair> M = new Stack<>();
            addSubpathsToStack(M, rootAccessPath, Set.of(StaticName.Anonymous().getStringName()));

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
            
        SchemaObject schemaObject = schema.getMorphism(dynamicName.signature()).cod();
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
        var morphism = schema.getMorphism(property.value().signature());
        
        if (morphism.isArray())
            wrapper.addSimpleArrayProperty(names, isRequired(morphism));
        else
            wrapper.addSimpleProperty(names, isRequired(morphism));
    }
    
    private void processPath(ComplexProperty property, Set<String> names)
    {
        var morphism = schema.getMorphism(property.signature());
        
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
