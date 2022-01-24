package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.record.*;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.utils.Debug;

import java.util.*;
import org.javatuples.Pair;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class ModelToCategory
{
    private SchemaCategory schema; // TODO
    //private InstanceCategory instance; // TODO
    private ForestOfRecords forest; // TODO
    private Mapping mapping; // TODO
    private ComplexProperty rootAccessPath;
            
    private InstanceFunctor instanceFunctor;
    
    public void input(SchemaCategory schema, InstanceCategory instance, ForestOfRecords forest, Mapping mapping)
    {
        this.schema = schema;
        //this.instance = instance;
        this.forest = forest;
        this.mapping = mapping;
        this.rootAccessPath = mapping.accessPath().copyWithoutAuxiliaryNodes();
        
        instanceFunctor = new InstanceFunctor(instance, schema);
    }
    
	public void algorithm()
    {
        if (Debug.shouldLog(4))
            System.out.println("# ALGORITHM");
        
        for (RootRecord rootRecord : forest)
        {
            if (Debug.shouldLog(4))
                System.out.println("################ Process of Root Record ################");
			// preparation phase
			Stack<StackTriple> M = mapping.hasRootMorphism() ?
                createStackWithMorphism(mapping.rootObject(), mapping.rootMorphism(), rootRecord) : // K with root morphism
                createStackWithObject(mapping.rootObject(), rootRecord); // K with root object

			// processing of the tree
			while (!M.empty())
				processTopOfStack(M);
		}
	}
    
    private Stack<StackTriple> createStackWithObject(SchemaObject object, RootRecord record)
    {
        if (Debug.shouldLog(3))
            System.out.println("#### Create Stack With Object ####");
        
        InstanceObject qI = instanceFunctor.object(object);
        IdWithValues sid = fetchSid(object.superId(), record);
        Stack<StackTriple> M = new Stack<>();
        
        ActiveDomainRow row = modify(qI, sid);
        addPathChildrenToStack(M, rootAccessPath, row, record);
        
        if (Debug.shouldLog(3))
            System.out.println("# Stack size: " + M.size());
        
        return M;
    }
    
    private Stack<StackTriple> createStackWithMorphism(SchemaObject object, SchemaMorphism morphism, RootRecord record)
    {
        if (Debug.shouldLog(3))
            System.out.println("#### Create Stack With Morphism ####");
        
        Stack<StackTriple> M = new Stack<>();
        
        InstanceObject qI_dom = instanceFunctor.object(object);
        IdWithValues sids_dom = fetchSid(object.superId(), record);
        ActiveDomainRow sid_dom = modify(qI_dom, sids_dom);

        SchemaObject qS_cod = morphism.cod();
        IdWithValues sids_cod = fetchSid(qS_cod.superId(), record);
        
        InstanceObject qI_cod = instanceFunctor.object(qS_cod);
        ActiveDomainRow sid_cod = modify(qI_cod, sids_cod);

        InstanceMorphism mI = instanceFunctor.morphism(morphism);

        addRelation(mI, sid_dom, sid_cod);
        addRelation(mI.dual(), sid_cod, sid_dom);

        AccessPath t_dom = rootAccessPath.getSubpathBySignature(Signature.Empty());
        AccessPath t_cod = rootAccessPath.getSubpathBySignature(morphism.signature());

        AccessPath ap = rootAccessPath.minusSubpath(t_dom).minusSubpath(t_cod);

        addPathChildrenToStack(M, ap, sid_dom, record);
        addPathChildrenToStack(M, t_cod, sid_cod, record);
        
        if (Debug.shouldLog(3))
            System.out.println("# Stack size: " + M.size());
        
        return M;
    }
    
    private void processTopOfStack(Stack<StackTriple> M)
    {
        if (Debug.shouldLog(3))
            System.out.println("#### Process Top of Stack ####");
        if (Debug.shouldLog(2))
            printStack(M);
        
        StackTriple triple = M.pop();
        InstanceMorphism mI = instanceFunctor.morphism(triple.mS);
        SchemaObject oS = triple.mS.cod();
        InstanceObject qI = instanceFunctor.object(oS);
        Iterable<Pair<IdWithValues, IComplexRecord>> sids = fetchSids(oS.superId(), triple.record, triple.pid, triple.mS);

        for (Pair<IdWithValues, IComplexRecord> sid : sids)
        {
            ActiveDomainRow row = modify(qI, sid.getValue0());

            addRelation(mI, triple.pid, row);
            addRelation(mI.dual(), row, triple.pid);
            
            addPathChildrenToStack(M, triple.t, row, sid.getValue1());
        }
    }
    
    private void printStack(Stack<StackTriple> M)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("\nSize: ").append(M.size());
        
        var array = M.toArray();
        for (int i = array.length - 1; i >= 0; i--)
            builder.append("\n").append(array[i]);
        builder.append("\n");
        
        System.out.println(builder.toString());
    }

    // Fetch id with values for given record
	//private IdWithValues fetchSid(Id superId, RootRecord record)
    private static IdWithValues fetchSid(Id superId, RootRecord rootRecord)
    {
        var builder = new IdWithValues.Builder();
        
        for (Signature signature : superId.signatures())
        {
            /*
            Object value = rootRecord.values().get(signature).getValue();
            if (value instanceof String stringValue)
                builder.add(signature, stringValue);
            */
            SimpleRecord<?> simpleRecord = rootRecord.getSimpleRecord(signature);
            if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
            {
                builder.add(signature, simpleValueRecord.getValue().toString());
            }
            else
            {
                throw new UnsupportedOperationException("FetchSid doesn't support array values.");
            }
        }
        
        return builder.build();
	}
    
    // Fetch id with values for given record.
    // The return value is a set of (Signature, String) for each Signature in superId and its corresponding value from record.
    // Actually, there can be multiple values in the record, so the set of sets is returned.
    private static Iterable<Pair<IdWithValues, IComplexRecord>> fetchSids(Id superId, IComplexRecord parentRecord, ActiveDomainRow parentRow, SchemaMorphism morphism)
    {
        List<Pair<IdWithValues, IComplexRecord>> output = new ArrayList<>();
        Signature signature = morphism.signature();

        // If the id is empty, the output ids with values will have only one tuple: (<signature>, <value>).
        // This means they represent either singular values (SimpleValueRecord) or a nested document without identifier (not auxilliary).
        if (superId.compareTo(Id.Empty()) == 0)
        {
            // Value is in the parent active domain row.
            if (parentRow.hasSignature(signature))
            {
                String valueFromParentRow = parentRow.getValue(signature);
                addSimpleValueToOutput(output, valueFromParentRow);
            }
            // There is simple value/array record with given signature in the parent record.
            else if (parentRecord.hasSimpleRecord(signature))
            {
                addSidsFromSimpleRecordToOutput(output, parentRecord.getSimpleRecord(signature), signature);
            }
            // There are complex records with given signature in the parent record.
            // They don't represent any (string) value so an unique identifier must be generated instead.
            // But their complex value will be processed later.
            else if (parentRecord.hasComplexRecords(signature))
            {
                List<? extends IComplexRecord> childRecords = parentRecord.getComplexRecords(signature);
                childRecords.stream().forEach(childRecord -> addSimpleValueToOutput(output, UniqueIdProvider.getNext(), childRecord));
            }
        }
        // The superId isn't empty so we need to find value for each signature in superId and return the tuples (<signature>, <value>).
        // Because there are multiple signatures in the superId, we are dealing with a complex property (resp. properties, ie. children of given parentRecord).
        else if (parentRecord.hasComplexRecords(signature))
        {
            List<? extends IComplexRecord> childRecords = parentRecord.getComplexRecords(signature);
            for (IComplexRecord childRecord : childRecords)
            {
                if (childRecord.hasDynamicChildren())
                    processDynamicComplexRecord(superId, parentRow, signature, childRecord, output);
                else
                    processNonDynamicComplexRecord(superId, parentRow, signature, childRecord, output);
            }
        }
        
        return output;
    }

    private static void addSidsFromSimpleRecordToOutput(List<Pair<IdWithValues, IComplexRecord>> output, SimpleRecord<?> simpleRecord, Signature signature)
    {
        if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
            addSimpleValueToOutput(output, simpleValueRecord.getValue().toString());
        else if (simpleRecord instanceof SimpleArrayRecord<?> simpleArrayRecord)
            simpleArrayRecord.getValues().stream()
                .forEach(valueObject -> addSimpleValueToOutput(output, valueObject.toString()));
    }

    private static void addSimpleValueToOutput(List<Pair<IdWithValues, IComplexRecord>> output, String value)
    {
        // Doesn't matter if there is null because the accessPath is also null so it isn't further traversed
        addSimpleValueToOutput(output, value, null);
    }

    private static void addSimpleValueToOutput(List<Pair<IdWithValues, IComplexRecord>> output, String value, IComplexRecord childRecord)
    {
        var builder = new IdWithValues.Builder();
        builder.add(Signature.Empty(), value);
        output.add(new Pair<>(builder.build(), childRecord));
    }

    private static void processDynamicComplexRecord(Id superId, ActiveDomainRow parentRow, Signature pathSignature, IComplexRecord childRecord, List<Pair<IdWithValues, IComplexRecord>> output)
    {
        for (IComplexRecord dynamicChild : childRecord.getDynamicChildren())
        {
            var builder = new IdWithValues.Builder();
            addNonDynamicSignaturesToBuilder(superId.signatures(), builder, parentRow, pathSignature, dynamicChild);
            output.add(new Pair<>(builder.build(), new DynamicRecordWrapper(childRecord, dynamicChild)));
        }
    }
    
    private static void processNonDynamicComplexRecord(Id superId, ActiveDomainRow parentRow, Signature pathSignature, IComplexRecord childRecord, List<Pair<IdWithValues, IComplexRecord>> output)
    {
        Set<Signature> dynamicSignatures = new TreeSet<>();
        Set<Signature> nonDynamicSignatures = new TreeSet<>();
        
        for (Signature signature : superId.signatures())
            if (childRecord.containsDynamicValue(signature))
                dynamicSignatures.add(signature);
            else
                nonDynamicSignatures.add(signature);
        
        if (!childRecord.hasDynamicValues())
        {
            var builder = new IdWithValues.Builder();
            addNonDynamicSignaturesToBuilder(nonDynamicSignatures, builder, parentRow, pathSignature, childRecord);
            output.add(new Pair<>(builder.build(), childRecord));
            return;
        }

        for (SimpleValueRecord<?> dynamicRecord : childRecord.getDynamicValues())
        {
            var builder = new IdWithValues.Builder();
            addNonDynamicSignaturesToBuilder(nonDynamicSignatures, builder, parentRow, pathSignature, childRecord);
            
            for (Signature signature : dynamicSignatures)
            {
                String value = dynamicRecord.signature().equals(signature) ? dynamicRecord.getValue().toString() : dynamicRecord.name().value();
                builder.add(signature, value);
            }

            output.add(new Pair<>(builder.build(), childRecord));
        }
    }

    private static void addNonDynamicSignaturesToBuilder(Set<Signature> signatures, IdWithValues.Builder builder, ActiveDomainRow parentRow, Signature pathSignature, IComplexRecord childRecord)
    {
        for (Signature signature : signatures)
        {
            var signatureInParentRow = signature.traverseThrough(pathSignature);

            // Why java still doesn't support output arguments?
            String value;
            if (signatureInParentRow == null)
            {
                SimpleRecord<?> simpleRecord = childRecord.getSimpleRecord(signature);
                if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
                    value = simpleValueRecord.getValue().toString();
                else if (childRecord.name() instanceof DynamicRecordName dynamicName && dynamicName.signature().equals(signature))
                    value = dynamicName.value();
                else
                    throw new UnsupportedOperationException("FetchSids doesn't support array values for complex records.");
            }
            else
                value = parentRow.getValue(signatureInParentRow);

            builder.add(signature, value);
        }
    }

    // Create ActiveDomainRow from given IdWithValues and add it to the instance object
	private static ActiveDomainRow modify(InstanceObject qI, IdWithValues sid)
    {
        Set<ActiveDomainRow> rows = new TreeSet<>();
        Set<IdWithValues> idsWithValues = getIdsWithValues(qI.schemaObject().ids(), sid);
        
        for (IdWithValues idWithValues : idsWithValues)
        {
            Map<IdWithValues, ActiveDomainRow> map = qI.activeDomain().get(idWithValues.id());
            if (map == null)
                continue;
            
            ActiveDomainRow row = map.get(idWithValues);
            if (row != null && !rows.contains(row))
                rows.add(row);
        }
        
        var builder = new IdWithValues.Builder();
        for (ActiveDomainRow row : rows)
            for (Signature signature : row.signatures())
                builder.add(signature, row.getValue(signature));
        
        for (Signature signature : sid.signatures())
            builder.add(signature, sid.map().get(signature));
        
        ActiveDomainRow newRow = new ActiveDomainRow(builder.build());
        for (IdWithValues idWithValues : idsWithValues)
        {
            Map<IdWithValues, ActiveDomainRow> map = qI.activeDomain().get(idWithValues.id());
            if (map == null)
            {
                map = new TreeMap<>();
                qI.activeDomain().put(idWithValues.id(), map);
            }
            map.put(idWithValues, newRow);
        }
        
        // TODO: předělat existující morfizmy
        // WARNING: může se stát, že tu sloučím více řádků do jednoho - potom bude nutné sloučit jejich morfizmy do jednoho, což se nejlépe vyřeší lazy algoritmem
        // právě až to bude potřeba
        // TODO: optimalizovat
        
        return newRow;
	}
    
    private static Set<IdWithValues> getIdsWithValues(Set<Id> ids, IdWithValues sid)
    {
        Set<IdWithValues> output = new TreeSet<>();
        for (Id id : ids)
        {
            var builder = new IdWithValues.Builder();
            boolean idIsInSuperId = true;
            for (Signature signature : id.signatures())
            {
                String value = sid.map().get(signature);
                if (value == null)
                {
                    idIsInSuperId = false;
                    break;
                }
                builder.add(signature, value);
            }
            if (idIsInSuperId)
                output.add(builder.build());
        }
        return output;
    }
    
    private static void addRelation(InstanceMorphism morphism, ActiveDomainRow sid_dom, ActiveDomainRow sid_cod)
    {
		morphism.addMapping(new ActiveMappingRow(sid_dom, sid_cod));
	}
    
    private void addPathChildrenToStack(Stack<StackTriple> stack, AccessPath path, ActiveDomainRow sid, IComplexRecord record)
    //private static void addPathChildrenToStack(Stack<StackTriple> stack, AccessPath path, ActiveDomainRow sid, IComplexRecord record)
    {
        if (path instanceof ComplexProperty complexPath)
            for (Pair<Signature, ComplexProperty> child: children(complexPath))
            {
                SchemaMorphism morphism = schema.signatureToMorphism(child.getValue0());
                stack.push(new StackTriple(sid, morphism, child.getValue1(), record));
            }
    }

    /**
     * Determine possible sub-paths to be traversed from this complex property (inner node of an access path).
     * According to the paper, this function should return pairs of (context, value). But value of such sub-path can only be an {@link ComplexProperty}.
     * Similarly, context must be a signature of a morphism.
     * @return set of pairs (morphism signature, complex property) of all possible sub-paths.
     */
	private static Collection<Pair<Signature, ComplexProperty>> children(ComplexProperty complexProperty)
    {
        if (Debug.shouldLog(0))
        {
            System.out.println("$ Children:");
            System.out.println(complexProperty);
        }
        
        final List<Pair<Signature, ComplexProperty>> output = new ArrayList<>();
        
        for (AccessPath subpath: complexProperty.subpaths())
        {
            output.addAll(process(subpath.name()));
            output.addAll(process(subpath.context(), subpath.value()));
        }
        
        return output;
	}
    
    /**
     * Process (name, context and value) according to the "process" function from the paper.
     * This function is divided to two parts - one for name and other for context and value.
     * @param name
     * @return see {@link #children()}
     */
    private static Collection<Pair<Signature, ComplexProperty>> process(Name name)
    {
        if (Debug.shouldLog(0))
        {
            System.out.println("$ Process name:");
            System.out.println(name);
        }
        
        if (name instanceof DynamicName dynamicName)
            return List.of(new Pair<>(dynamicName.signature(), ComplexProperty.Empty()));
        else // Static or anonymous (empty) name
            return Collections.<Pair<Signature, ComplexProperty>>emptyList();
    }
    
    private static Collection<Pair<Signature, ComplexProperty>> process(IContext context, IValue value)
    {
        if (Debug.shouldLog(0))
        {
            System.out.println("$ Process context, value:");
            System.out.println(context);
            System.out.println(value);
        }
        
        if (value instanceof SimpleValue simpleValue)
        {
            final Signature contextSignature = context instanceof Signature signature ? signature : Signature.Empty();
            final Signature newSignature = simpleValue.signature().concatenate(contextSignature);
            
            return List.of(new Pair<>(newSignature, ComplexProperty.Empty()));
        }
        
        if (value instanceof ComplexProperty complexProperty)
        {
            if (context instanceof Signature signature)
                return List.of(new Pair<>(signature, complexProperty));
            else
                return children(complexProperty);
        }
        
        throw new UnsupportedOperationException();
    }
}
