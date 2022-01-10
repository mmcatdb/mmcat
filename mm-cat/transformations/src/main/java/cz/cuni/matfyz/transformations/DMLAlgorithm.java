package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.statements.DMLStatement;

import java.util.*;
import org.javatuples.Pair;

/**
 *
 * @author jachymb.bartik
 */
public class DMLAlgorithm
{
    /*
    private SchemaCategory schema; // TODO
    private InstanceFunctor instanceFunctor;
    private String name; // TODO
    private Mapping mapping;
    private AbstractPushWrapper wrapper;

    public void input(SchemaCategory schema, InstanceCategory instance, String name, Mapping mapping, AbstractPushWrapper wrapper)
    {
        this.schema = schema;
        instanceFunctor = new InstanceFunctor(instance, schema);
        this.name = name;
        this.mapping = mapping;
        this.wrapper = wrapper;
    }
    
    public List<DMLStatement> algorithm() throws Exception
    {
        return mapping.hasRootMorphism() ?
            processWithMorphism(mapping.rootMorphism()) : // K with root morphism
            processWithObject(mapping.rootObject()); // K with root object
    }

    private List<DMLStatement> processWithObject(SchemaObject object) throws Exception
    {
        InstanceObject qI = instanceFunctor.object(object);
        List<ActiveDomainRow> S = fetchSids(qI);
        Stack<DMLStackTriple> M = new Stack<>();
        List<DMLStatement> output = new ArrayList<>();

        for (ActiveDomainRow sid : S)
        {
            M.push(new DMLStackTriple(sid, Name.Anonymous().getStringName(), mapping.accessPath()));
            output.add(buildStatement(M));
        }

        return output;
    }

    private List<DMLStatement> processWithMorphism(SchemaMorphism morphism) throws Exception
    {
        InstanceMorphism mI = instanceFunctor.morphism(morphism);
        List<ActiveMappingRow> S = fetchRelations(mI);
        AccessPath codomainPath = mapping.accessPath().getSubpathBySignature(morphism.signature());
        Stack<DMLStackTriple> M = new Stack<>();
        List<DMLStatement> output = new ArrayList<>();

        for (ActiveMappingRow row : S)
        {
            M.push(new DMLStackTriple(row.domainRow(), Name.Anonymous().getStringName(), mapping.accessPath().minusSubpath(codomainPath)));
            M.push(new DMLStackTriple(row.codomainRow(), Name.Anonymous().getStringName(), codomainPath));
            output.add(buildStatement(M));
        }

        return output;
    }

    private List<ActiveDomainRow> fetchSids(InstanceObject object)
    {

    }

    private List<ActiveMappingRow> fetchRelations(InstanceMorphism morphism)
    {

    }

    private DMLStatement buildStatement(Stack<DMLStackTriple> M)
    {
        wrapper.clear();
        wrapper.setKindName(name.getStringName());

        while (!M.empty())
        {
            DMLStackTriple triple = M.pop();
            List<Pair<String, IValue>> pairs = collectNameValuePairs(triple.t, triple.pid);

            for (var pair : pairs)
            {
                String newName = triple.name.getStringName() + "/" + pair.getValue0().getStringName();

                if (pair.getValue1() instanceof SimpleProperty simpleProperty)
                    wrapper.append(newName, simpleProperty);
                else if (pair.getValue1() instanceof ComplexProperty complexProperty)
                    for (AccessPath subpath : complexProperty.subpaths())
                    M.push(new DMLStackTriple(complexProperty, newName, subpath));
            }
        }

    }

    private List<Pair<String, IValue>> collectNameValuePairs(AccessPath path, ActiveMappingRow row)
    {
        
    }
    */
}
