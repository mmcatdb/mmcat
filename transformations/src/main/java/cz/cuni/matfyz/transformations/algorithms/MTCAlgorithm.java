package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.record.*;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.category.Signature.Type;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;

import java.util.*;
import org.javatuples.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class MTCAlgorithm
{
    private static Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithm.class);

    private ForestOfRecords forest;
    private Mapping mapping;
    private InstanceCategory instance;
    
    public void input(Mapping mapping, InstanceCategory instance, ForestOfRecords forest)
    {
        this.forest = forest;
        this.mapping = mapping;
        this.instance = instance;
    }
    
    public void algorithm()
    {
        LOGGER.debug("Model To Category algorithm");
        final ComplexProperty rootAccessPath = mapping.accessPath().copyWithoutAuxiliaryNodes();
        
        for (RootRecord rootRecord : forest)
            processRootRecord(rootRecord, rootAccessPath);
    }

    private void processRootRecord(RootRecord rootRecord, ComplexProperty rootAccessPath) {
        LOGGER.debug("Process a root record:\n{}", rootRecord);

        // preparation phase
        Stack<StackTriple> M = mapping.hasRootMorphism() ?
        createStackWithMorphism(mapping.rootObject(), mapping.rootMorphism(), rootRecord, rootAccessPath) : // K with root morphism
        createStackWithObject(mapping.rootObject(), rootRecord, rootAccessPath); // K with root object

        // processing of the tree
        while (!M.empty())
            processTopOfStack(M);
    }
    
    private Stack<StackTriple> createStackWithObject(SchemaObject object, RootRecord record, ComplexProperty rootAccessPath)
    {
        InstanceObject qI = instance.getObject(object);
        IdWithValues sid = fetchSid(object.superId(), record);
        Stack<StackTriple> M = new Stack<>();
        
        ActiveDomainRow row = modifyActiveDomain(qI, sid);
        addPathChildrenToStack(M, rootAccessPath, row, record);
        
        return M;
    }
    
    private Stack<StackTriple> createStackWithMorphism(SchemaObject object, SchemaMorphism morphism, RootRecord record, ComplexProperty rootAccessPath)
    {
        Stack<StackTriple> M = new Stack<>();
        
        InstanceObject qI_dom = instance.getObject(object);
        IdWithValues sids_dom = fetchSid(object.superId(), record);
        ActiveDomainRow sid_dom = modifyActiveDomain(qI_dom, sids_dom);

        SchemaObject qS_cod = morphism.cod();
        IdWithValues sids_cod = fetchSid(qS_cod.superId(), record);
        
        InstanceObject qI_cod = instance.getObject(qS_cod);
        ActiveDomainRow sid_cod = modifyActiveDomain(qI_cod, sids_cod);

        InstanceMorphism mI = instance.getMorphism(morphism);

        addRelation(mI, sid_dom, sid_cod, record);
        addRelation(mI.dual(), sid_cod, sid_dom, record);

        AccessPath t_dom = rootAccessPath.getSubpathBySignature(Signature.Empty());
        AccessPath t_cod = rootAccessPath.getSubpathBySignature(morphism.signature());

        AccessPath ap = rootAccessPath.minusSubpath(t_dom).minusSubpath(t_cod);

        addPathChildrenToStack(M, ap, sid_dom, record);
        addPathChildrenToStack(M, t_cod, sid_cod, record);
        
        return M;
    }
    
    private void processTopOfStack(Stack<StackTriple> M)
    {
        LOGGER.debug("Process Top of Stack:\n{}", M);
        
        StackTriple triple = M.pop();
        Iterable<FetchedSuperId> sids = fetchSids(triple.parentRecord, triple.parentRow, triple.parentToChildMorphism);

        SchemaObject childObject = triple.parentToChildMorphism.cod();
        InstanceObject childInstance = instance.getObject(childObject);
        InstanceMorphism morphismInstance = instance.getMorphism(triple.parentToChildMorphism);
        
        for (FetchedSuperId sid : sids) {
            ActiveDomainRow childRow = modifyActiveDomain(childInstance, sid.idWithValues);

            addRelation(morphismInstance, triple.parentRow, childRow, sid.childRecord);
            addRelation(morphismInstance.dual(), childRow, triple.parentRow, sid.childRecord);
            
            addPathChildrenToStack(M, triple.t, childRow, sid.childRecord);
        }
    }

    // Fetch id-with-values for given root record.
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
                LOGGER.warn("A simple record with signature " + signature + " is an array record:\n" + simpleRecord + "\n");
                throw new UnsupportedOperationException("FetchSid doesn't support array values.");
            }
        }
        
        return builder.build();
    }
    
    /**
     * Fetch id-with-values for a schema object in given record / domain row.
     * The output is a set of (Signature, String) for each Signature in superId and its corresponding value from record. Actually, there can be multiple values in the record, so a list of these sets is returned.
     * For further processing, the child records associated with the values are needed (if they are complex), so they are added to the output as well.
     * @param parentRecord Record of the parent (in the access path) schema object.
     * @param parentRow Active domain row of the parent schema object.
     * @param morphism Morphism from the parent schema object to the currently processed one.
     * @return
     */
    private static Iterable<FetchedSuperId> fetchSids(IComplexRecord parentRecord, ActiveDomainRow parentRow, SchemaMorphism morphism)
    {
        List<FetchedSuperId> output = new ArrayList<>();
        Id superId = morphism.cod().superId();
        Signature signature = morphism.signature();

        // If the id is empty, the output ids with values will have only one tuple: (<signature>, <value>).
        // This means they represent either singular values (SimpleValueRecord) or a nested document without identifier (not auxilliary).
        if (superId.equals(Id.Empty()))
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
                for (IComplexRecord childRecord : parentRecord.getComplexRecords(signature))
                    addSimpleValueWithChildRecordToOutput(output, UniqueIdProvider.getNext(), childRecord);
            }
        }
        // The superId isn't empty so we need to find value for each signature in superId and return the tuples (<signature>, <value>).
        // Because there are multiple signatures in the superId, we are dealing with a complex property (resp. properties, i.e., children of given parentRecord).
        else if (parentRecord.hasComplexRecords(signature)) {
            for (IComplexRecord childRecord : parentRecord.getComplexRecords(signature)) {
                // If the record has children/values with dynamic names for a signature, it is not allowed to have any other children/values (even with static names) for any other signature.
                // So there are two different complex records - one with static children/values (with possibly different signatures) and the other with only dynamic ones (with the same signature).
                if (childRecord.hasDynamicNameChildren())
                    processComplexRecordWithDynamicChildren(output, superId, parentRow, signature, childRecord);
                else if (childRecord.hasDynamicNameValues())
                    processComplexRecordWithDynamicValues(output, superId, parentRow, signature, childRecord);
                else
                    processStaticComplexRecord(output, superId, parentRow, signature, childRecord);
            }
        }
        
        return output;
    }

    private static void addSidsFromSimpleRecordToOutput(List<FetchedSuperId> output, SimpleRecord<?> simpleRecord, Signature signature)
    {
        if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
            addSimpleValueToOutput(output, simpleValueRecord.getValue().toString());
        else if (simpleRecord instanceof SimpleArrayRecord<?> simpleArrayRecord)
            simpleArrayRecord.getValues().stream()
                .forEach(valueObject -> addSimpleValueToOutput(output, valueObject.toString()));
    }

    private static void addSimpleValueToOutput(List<FetchedSuperId> output, String value)
    {
        // It doesn't matter if there is null because the accessPath is also null so it isn't further traversed
        addSimpleValueWithChildRecordToOutput(output, value, null);
    }

    private static void addSimpleValueWithChildRecordToOutput(List<FetchedSuperId> output, String value, IComplexRecord childRecord)
    {
        var builder = new IdWithValues.Builder();
        builder.add(Signature.Empty(), value);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private static void processComplexRecordWithDynamicChildren(List<FetchedSuperId> output, Id superId, ActiveDomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (IComplexRecord dynamicNameChild : childRecord.getDynamicNameChildren()) {
            var builder = new IdWithValues.Builder();
            addStaticNameSignaturesToBuilder(superId.signatures(), builder, parentRow, pathSignature, dynamicNameChild);
            output.add(new FetchedSuperId(builder.build(), new DynamicRecordWrapper(childRecord, dynamicNameChild)));
        }
    }

    private static void processComplexRecordWithDynamicValues(List<FetchedSuperId> output, Id superId, ActiveDomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (SimpleValueRecord<?> dynamicNameValue : childRecord.getDynamicNameValues()) {
            var builder = new IdWithValues.Builder();
            Set<Signature> staticNameSignatures = new TreeSet<>();

            for (Signature signature : superId.signatures()) {
                if (dynamicNameValue.signature().equals(signature))
                    builder.add(signature, dynamicNameValue.getValue().toString());
                else if (dynamicNameValue.name() instanceof DynamicRecordName dynamicName && dynamicName.signature().equals(signature))
                    builder.add(signature, dynamicNameValue.name().value());
                // If the signature is not the dynamic value nor its dynamic name, it is static and we have to find it elsewhere.
                else
                    staticNameSignatures.add(signature);
            }

            addStaticNameSignaturesToBuilder(staticNameSignatures, builder, parentRow, pathSignature, childRecord);

            output.add(new FetchedSuperId(builder.build(), childRecord));
        }
    }
    
    private static void processStaticComplexRecord(List<FetchedSuperId> output, Id superId, ActiveDomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        var builder = new IdWithValues.Builder();
        addStaticNameSignaturesToBuilder(superId.signatures(), builder, parentRow, pathSignature, childRecord);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private static void addStaticNameSignaturesToBuilder(Set<Signature> signatures, IdWithValues.Builder builder, ActiveDomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (Signature signature : signatures) {
            // How the signature looks like from the parent object.
            var signatureInParentRow = signature.traverseThrough(pathSignature);

            // Why java still doesn't support output arguments?
            String value;
            if (signatureInParentRow == null) {
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

    // Creates ActiveDomainRow from given IdWithValues and adds it to the instance object.
    private static ActiveDomainRow modifyActiveDomain(InstanceObject instanceObject, IdWithValues superIdWithValues) {
        Set<ActiveDomainRow> rows = new TreeSet<>();
        Set<IdWithValues> subsetIdsWithValues = getSubsetIdsWithValues(instanceObject.schemaObject().ids(), superIdWithValues);
        
        // We find all rows that are identified by the superIdWithValues.
        for (IdWithValues idWithValues : subsetIdsWithValues) {
            Map<IdWithValues, ActiveDomainRow> map = instanceObject.activeDomain().get(idWithValues.id());
            if (map == null)
                continue;
            
            ActiveDomainRow row = map.get(idWithValues);
            if (row != null && !rows.contains(row))
                rows.add(row);
        }
        
        // We merge them together.
        var builder = new IdWithValues.Builder();
        for (ActiveDomainRow row : rows)
            for (Signature signature : row.signatures())
                builder.add(signature, row.getValue(signature));
        
        for (Signature signature : superIdWithValues.signatures())
            builder.add(signature, superIdWithValues.map().get(signature));
        
        ActiveDomainRow newRow = new ActiveDomainRow(builder.build());
        for (IdWithValues subsetIdWithValues : subsetIdsWithValues) {
            Map<IdWithValues, ActiveDomainRow> map = instanceObject.activeDomain().get(subsetIdWithValues.id());
            if (map == null) {
                map = new TreeMap<>();
                instanceObject.activeDomain().put(subsetIdWithValues.id(), map);
            }
            map.put(subsetIdWithValues, newRow);
        }
        
        // TODO: The update of the already existing morphisms should be optimized.
        
        return newRow;
    }
    
    // Returns all ids that are contained in given idWithValues as a subset (with their values from the superIdWithValues).
    private static Set<IdWithValues> getSubsetIdsWithValues(Set<Id> ids, IdWithValues superIdWithValues) {
        Set<IdWithValues> output = new TreeSet<>();
        // For each possible Id from ids, we find if superIdWithValues contains all its signatures (i.e., if superIdWithValues.signatures() is a superset of id.signatures()).
        for (Id id : ids) {
            var builder = new IdWithValues.Builder();

            boolean idIsInSuperId = true;
            for (Signature signature : id.signatures()) {
                String value = superIdWithValues.map().get(signature);
                if (value == null) {
                    idIsInSuperId = false;
                    break;
                }
                builder.add(signature, value);
            }
            // If so, we add the Id (with its corresponding values) to the output.
            if (idIsInSuperId)
                output.add(builder.build());
        }
        return output;
    }
    
    private void addRelation(InstanceMorphism morphism, ActiveDomainRow sid_dom, ActiveDomainRow sid_cod, IComplexRecord record)
    {
        morphism.addMapping(new ActiveMappingRow(sid_dom, sid_cod));
        if (morphism.signature().getType() != Type.COMPOSITE)
            return;

        var a = morphism.signature().toString();
        var b = sid_dom.toString();
        var c = sid_cod.toString();
        if (record != null) {
            var d = record.toString();
        }

        // Split to base morphisms and fill them.
        // We have to either find the corresponding objects on the path from the record,
        // or create them with a technical identifier (autoincrement).
        /* TODO
        var baseMorphisms = morphism.signature().toBases().stream().map(signature -> instance.getMorphism(signature)).toList();
        var currentRow = sid_dom;
        for (var baseMorphism : baseMorphisms) {
            record.
        }
        */
    }
    
    private void addPathChildrenToStack(Stack<StackTriple> stack, AccessPath path, ActiveDomainRow sid, IComplexRecord record)
    //private static void addPathChildrenToStack(Stack<StackTriple> stack, AccessPath path, ActiveDomainRow sid, IComplexRecord record)
    {
        if (path instanceof ComplexProperty complexPath)
            for (Pair<Signature, ComplexProperty> child: children(complexPath))
            {
                SchemaMorphism morphism = instance.getMorphism(child.getValue0()).schemaMorphism();
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
        if (name instanceof DynamicName dynamicName)
            return List.of(new Pair<>(dynamicName.signature(), ComplexProperty.Empty()));
        else // Static or anonymous (empty) name
            return Collections.<Pair<Signature, ComplexProperty>>emptyList();
    }
    
    private static Collection<Pair<Signature, ComplexProperty>> process(IContext context, IValue value)
    {
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
