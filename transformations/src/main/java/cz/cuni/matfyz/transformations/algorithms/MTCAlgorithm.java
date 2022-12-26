package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.Merger;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.IContext;
import cz.cuni.matfyz.core.mapping.IValue;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.mapping.SimpleValue;
import cz.cuni.matfyz.core.record.DynamicRecordName;
import cz.cuni.matfyz.core.record.DynamicRecordWrapper;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.IComplexRecord;
import cz.cuni.matfyz.core.record.RootRecord;
import cz.cuni.matfyz.core.record.SimpleArrayRecord;
import cz.cuni.matfyz.core.record.SimpleRecord;
import cz.cuni.matfyz.core.record.SimpleValueRecord;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.schema.SignatureId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class MTCAlgorithm {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithm.class);

    private ForestOfRecords forest;
    private Mapping mapping;
    private InstanceCategory category;
    
    public void input(Mapping mapping, InstanceCategory category, ForestOfRecords forest) {
        this.forest = forest;
        this.mapping = mapping;
        this.category = category;
    }
    
    public void algorithm() {
        LOGGER.debug("Model To Category algorithm");
        final ComplexProperty rootAccessPath = mapping.accessPath().copyWithoutAuxiliaryNodes();

        // Create references for adding found values to the superId of other rows.
        category.createReferences();

        for (RootRecord rootRecord : forest)
            processRootRecord(rootRecord, rootAccessPath);
    }

    private void processRootRecord(RootRecord rootRecord, ComplexProperty rootAccessPath) {
        LOGGER.debug("Process a root record:\n{}", rootRecord);

        // preparation phase
        Deque<StackTriple> masterStack = mapping.hasRootMorphism()
            ? createStackWithMorphism(mapping.rootObject(), mapping.rootMorphism(), rootRecord, rootAccessPath) // K with root morphism
            : createStackWithObject(mapping.rootObject(), rootRecord, rootAccessPath); // K with root object

        // processing of the tree
        while (!masterStack.isEmpty())
            processTopOfStack(masterStack);
    }
    
    private Deque<StackTriple> createStackWithObject(SchemaObject object, RootRecord rootRecord, ComplexProperty rootAccessPath) {
        InstanceObject instanceObject = category.getObject(object);
        SuperIdWithValues superId = fetchSuperId(object.superId(), rootRecord);
        Deque<StackTriple> masterStack = new LinkedList<>();
        
        DomainRow row = modifyActiveDomain(instanceObject, superId);
        addPathChildrenToStack(masterStack, rootAccessPath, row, rootRecord);
        
        return masterStack;
    }
    
    private Deque<StackTriple> createStackWithMorphism(SchemaObject object, SchemaMorphism morphism, RootRecord rootRecord, ComplexProperty rootAccessPath) {
        Deque<StackTriple> masterStack = new LinkedList<>();
        
        InstanceObject domainInstance = category.getObject(object);
        SuperIdWithValues domainSuperId = fetchSuperId(object.superId(), rootRecord);
        DomainRow domainRow = modifyActiveDomain(domainInstance, domainSuperId);

        SchemaObject codomainObject = morphism.cod();
        InstanceObject codomainInstance = category.getObject(codomainObject);
        SuperIdWithValues codomainSuperId = fetchSuperId(codomainObject.superId(), rootRecord);
        DomainRow codomainRow = modifyActiveDomain(codomainInstance, codomainSuperId);

        InstanceMorphism morphismInstance = category.getMorphism(morphism);

        addRelation(morphismInstance, domainRow, codomainRow, rootRecord);
        addRelation(morphismInstance.dual(), codomainRow, domainRow, rootRecord);

        AccessPath domainSubpath = rootAccessPath.getSubpathBySignature(Signature.createEmpty());
        AccessPath codomainSubpath = rootAccessPath.getSubpathBySignature(morphism.signature());

        AccessPath restAccessPath = rootAccessPath.minusSubpath(domainSubpath).minusSubpath(codomainSubpath);

        addPathChildrenToStack(masterStack, restAccessPath, domainRow, rootRecord);
        addPathChildrenToStack(masterStack, codomainSubpath, codomainRow, rootRecord);
        
        return masterStack;
    }
    
    private void processTopOfStack(Deque<StackTriple> masterStack) {
        //LOGGER.debug("Process Top of Stack:\n{}", masterStack);
        
        StackTriple triple = masterStack.pop();
        Iterable<FetchedSuperId> superIds = fetchSuperIds(triple.parentRecord, triple.parentRow, triple.parentToChildMorphism);

        InstanceObject childInstance = triple.parentToChildMorphism.cod();
        
        for (FetchedSuperId superId : superIds) {
            DomainRow childRow = modifyActiveDomain(childInstance, superId.superId);
            childRow = addRelation(triple.parentToChildMorphism, triple.parentRow, childRow, triple.parentRecord);

            //childInstance.merge(childRow);
            
            addPathChildrenToStack(masterStack, triple.parentAccessPath, childRow, superId.childRecord);
        }
    }

    // Fetch id-with-values for given root record.
    private static SuperIdWithValues fetchSuperId(SignatureId superId, RootRecord rootRecord) {
        var builder = new SuperIdWithValues.Builder();
        
        for (Signature signature : superId.signatures()) {
            /*
            Object value = rootRecord.values().get(signature).getValue();
            if (value instanceof String stringValue)
                builder.add(signature, stringValue);
            */
            SimpleRecord<?> simpleRecord = rootRecord.getSimpleRecord(signature);
            if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord) {
                builder.add(signature, simpleValueRecord.getValue().toString());
            }
            else {
                LOGGER.warn("A simple record with signature {} is an array record:\n{}\n", signature, simpleRecord);
                throw new UnsupportedOperationException("FetchSuperId doesn't support array values.");
            }
        }
        
        return builder.build();
    }
    
    /**
     * Fetch id-with-values for a schema object in given record / domain row.
     * The output is a set of (Signature, String) for each Signature in superId and its corresponding value from record. Actually, there can be multiple values in the record, so a list of these sets is returned.
     * For further processing, the child records associated with the values are needed (if they are complex), so they are added to the output as well.
     * @param parentRecord Record of the parent (in the access path) schema object.
     * @param parentRow Domain row of the parent schema object.
     * @param morphism Morphism from the parent schema object to the currently processed one.
     * @return
     */
    private static Iterable<FetchedSuperId> fetchSuperIds(IComplexRecord parentRecord, DomainRow parentRow, InstanceMorphism morphism) {
        List<FetchedSuperId> output = new ArrayList<>();
        SignatureId superId = morphism.cod().superId();
        Signature signature = morphism.signature();

        if (superId.hasOnlyEmptySignature()) {
            // If the id is empty, the output ids with values will have only one tuple: (<signature>, <value>).
            // This means they represent either singular values (SimpleValueRecord) or a nested document without identifier (not auxilliary).
            if (parentRow.hasSignature(signature)) {
                // Value is in the parent domain row.
                String valueFromParentRow = parentRow.getValue(signature);
                addSimpleValueToOutput(output, valueFromParentRow);
            }
            else if (parentRecord.hasSimpleRecord(signature)) {
                // There is simple value/array record with given signature in the parent record.
                addSuperIdsFromSimpleRecordToOutput(output, parentRecord.getSimpleRecord(signature));
            }
            else if (parentRecord.hasComplexRecords(signature)) {
                // There are complex records with given signature in the parent record.
                // They don't represent any (string) value so an unique identifier must be generated instead.
                // But their complex value will be processed later.
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

    private static void addSuperIdsFromSimpleRecordToOutput(List<FetchedSuperId> output, SimpleRecord<?> simpleRecord) {
        if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
            addSimpleValueToOutput(output, simpleValueRecord.getValue().toString());
        else if (simpleRecord instanceof SimpleArrayRecord<?> simpleArrayRecord)
            simpleArrayRecord.getValues().stream()
                .forEach(valueObject -> addSimpleValueToOutput(output, valueObject.toString()));
    }

    private static void addSimpleValueToOutput(List<FetchedSuperId> output, String value) {
        // It doesn't matter if there is null because the accessPath is also null so it isn't further traversed
        addSimpleValueWithChildRecordToOutput(output, value, null);
    }

    private static void addSimpleValueWithChildRecordToOutput(List<FetchedSuperId> output, String value, IComplexRecord childRecord) {
        var builder = new SuperIdWithValues.Builder();
        builder.add(Signature.createEmpty(), value);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private static void processComplexRecordWithDynamicChildren(List<FetchedSuperId> output, SignatureId superId, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (IComplexRecord dynamicNameChild : childRecord.getDynamicNameChildren()) {
            var builder = new SuperIdWithValues.Builder();
            addStaticNameSignaturesToBuilder(superId.signatures(), builder, parentRow, pathSignature, dynamicNameChild);
            output.add(new FetchedSuperId(builder.build(), new DynamicRecordWrapper(childRecord, dynamicNameChild)));
        }
    }

    private static void processComplexRecordWithDynamicValues(List<FetchedSuperId> output, SignatureId superId, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (SimpleValueRecord<?> dynamicNameValue : childRecord.getDynamicNameValues()) {
            var builder = new SuperIdWithValues.Builder();
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
    
    private static void processStaticComplexRecord(List<FetchedSuperId> output, SignatureId superId, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        var builder = new SuperIdWithValues.Builder();
        addStaticNameSignaturesToBuilder(superId.signatures(), builder, parentRow, pathSignature, childRecord);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private static void addStaticNameSignaturesToBuilder(Set<Signature> signatures, SuperIdWithValues.Builder builder, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
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
                    throw new UnsupportedOperationException("FetchSuperIds doesn't support array values for complex records.");
            }
            else
                value = parentRow.getValue(signatureInParentRow);

            builder.add(signature, value);
        }
    }

    /**
     * Creates DomainRow from given SuperIdWithValues, adds it to the instance object and merges it with other potentially duplicite rows.
     * @param instanceObject
     * @param superId
     * @return
     */
    private static DomainRow modifyActiveDomain(InstanceObject instanceObject, SuperIdWithValues superId) {
        var merger = new Merger();
        return merger.merge(superId, instanceObject);
    }

    private DomainRow addRelation(InstanceMorphism morphism, DomainRow parentRow, DomainRow childRow, IComplexRecord childRecord) {
        // First, create a domain row with technical id for each object between the domain and the codomain objects on the path of the morphism.
        var baseMorphisms = morphism.bases();
        var currentDomainRow = parentRow;

        var parentToCurrent = Signature.createEmpty();
        var currentToChild = morphism.signature();

        for (var baseMorphism : baseMorphisms) {
            var instanceObject = baseMorphism.cod();
            
            parentToCurrent = parentToCurrent.concatenate(currentToChild.getFirst());
            currentToChild = currentToChild.cutFirst();
            
            // If we are not at the end of the morphisms, we have to create (or get, if it exists) a new row.
            if (!instanceObject.equals(morphism.cod())) {
                var superId = fetchSuperIdForTechnicalRow(instanceObject, parentRow, parentToCurrent.dual(), childRow, currentToChild, childRecord);
                currentDomainRow = instanceObject.getOrCreateRowWithMorphism(superId, currentDomainRow, baseMorphism);
            }
            else {
                baseMorphism.createMappingWithDual(currentDomainRow, childRow);
                // TODO both rows might need to reference the rows to which they are connected by the baseMorphism. Although it is rare, it should be adressed. See a similar comment in the Merger::createNewMappingsForMorphism() function.
            }
        }

        // Now try merging them from the codomain object to the domain object (the other way should be already merged).
        var merger = new Merger();
        childRow = merger.mergeAlongMorphism(childRow, baseMorphisms.get(baseMorphisms.size() - 1).dual());

        return childRow;
    }

    private SuperIdWithValues fetchSuperIdForTechnicalRow(InstanceObject instanceObject, DomainRow parentRow, Signature pathToParent, DomainRow childRow, Signature pathToChild, IComplexRecord parentRecord) {
        var builder = new SuperIdWithValues.Builder();

        for (var signature : instanceObject.superId().signatures()) {
            // The value is in either the first row ...
            var signatureInFirstRow = signature.traverseAlong(pathToParent);
            if (parentRow.hasSignature(signatureInFirstRow)) {
                builder.add(signature, parentRow.getValue(signatureInFirstRow));
                continue;
            }
            
            var signatureInLastRow = signature.traverseAlong(pathToChild);
            if (childRow.hasSignature(signatureInLastRow)) {
                builder.add(signature, childRow.getValue(signatureInLastRow));
                continue;
            }

            // TODO find the value in parent record
        }

        return builder.build();
    }

    private void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, DomainRow superId, IComplexRecord complexRecord) {
        //private static void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, ActiveDomainRow superId, IComplexRecord record) {
        if (path instanceof ComplexProperty complexPath)
            for (Child child : children(complexPath)) {
                InstanceMorphism morphism = category.getMorphism(child.signature());
                stack.push(new StackTriple(superId, morphism, child.property(), complexRecord));
            }
    }

    private record Child(Signature signature, ComplexProperty property) {}

    /**
     * Determine possible sub-paths to be traversed from this complex property (inner node of an access path).
     * According to the paper, this function should return pairs of (context, value). But value of such sub-path can only be an {@link ComplexProperty}.
     * Similarly, context must be a signature of a morphism.
     * @return set of pairs (morphism signature, complex property) of all possible sub-paths.
     */
    private static Collection<Child> children(ComplexProperty complexProperty) {
        final List<Child> output = new ArrayList<>();
        
        for (AccessPath subpath : complexProperty.subpaths()) {
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
    private static Collection<Child> process(Name name) {
        if (name instanceof DynamicName dynamicName)
            return List.of(new Child(dynamicName.signature(), ComplexProperty.createEmpty()));
        else // Static or anonymous (empty) name
            return Collections.<Child>emptyList();
    }
    
    private static Collection<Child> process(IContext context, IValue value) {
        if (value instanceof SimpleValue simpleValue) {
            final Signature contextSignature = context instanceof Signature signature ? signature : Signature.createEmpty();
            final Signature newSignature = simpleValue.signature().concatenate(contextSignature);
            
            return List.of(new Child(newSignature, ComplexProperty.createEmpty()));
        }
        
        if (value instanceof ComplexProperty complexProperty) {
            if (context instanceof Signature signature)
                return List.of(new Child(signature, complexProperty));
            else
                return children(complexProperty);
        }
        
        throw new UnsupportedOperationException("Process");
    }
}
