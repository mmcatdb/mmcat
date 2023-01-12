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
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.IComplexRecord;
import cz.cuni.matfyz.core.record.RootRecord;
import cz.cuni.matfyz.core.record.SimpleRecord;
import cz.cuni.matfyz.core.record.SimpleValueRecord;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.schema.SignatureId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

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
        /*
        Deque<StackTriple> masterStack = mapping.hasRootMorphism()
            ? createStackWithMorphism(mapping.rootObject(), mapping.rootMorphism(), rootRecord, rootAccessPath) // K with root morphism
            : createStackWithObject(mapping.rootObject(), rootRecord, rootAccessPath); // K with root object
        */
        Deque<StackTriple> masterStack = createStackWithObject(mapping.rootObject(), rootRecord, rootAccessPath);

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
    
    /*
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
    */
    
    private void processTopOfStack(Deque<StackTriple> masterStack) {
        //LOGGER.debug("Process Top of Stack:\n{}", masterStack);
        
        StackTriple triple = masterStack.pop();
        final var superIds = SuperIdsFetcher.fetch(triple.parentRecord, triple.parentRow, triple.parentToChild, triple.childAccessPath);

        InstanceObject childInstance = triple.parentToChild.cod();
        
        for (final var superId : superIds) {
            DomainRow childRow = modifyActiveDomain(childInstance, superId.superId());
            childRow = addRelation(triple.parentToChild, triple.parentRow, childRow, triple.parentRecord);

            //childInstance.merge(childRow);
            
            addPathChildrenToStack(masterStack, triple.childAccessPath, childRow, superId.childRecord());
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
        final var baseMorphisms = morphism.bases();
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
                currentDomainRow = InstanceObject.getOrCreateRowWithBaseMorphism(superId, currentDomainRow, baseMorphism);
            }
            else {
                baseMorphism.createMappingWithDual(currentDomainRow, childRow);
                // TODO both rows might need to reference the rows to which they are connected by the baseMorphism. Although it is rare, it should be adressed. See a similar comment in the Merger::createNewMappingsForMorphism() function.
            }
        }

        // Now try merging them from the codomain object to the domain object (the other way should be already merged).
        final var merger = new Merger();
        return merger.mergeAlongMorphism(childRow, baseMorphisms.get(baseMorphisms.size() - 1).dual());
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

    private void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, DomainRow parentRow, IComplexRecord complexRecord) {
        //private static void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, ActiveDomainRow superId, IComplexRecord record) {
        if (path instanceof ComplexProperty complexPath)
            for (Child child : children(complexPath)) {
                InstanceMorphism parentToChild = category.getMorphism(child.signature());
                stack.push(new StackTriple(parentRow, parentToChild, child.property(), complexRecord));
            }
    }

    //private record Child(Signature signature, ComplexProperty property) {}
    private record Child(Signature signature, AccessPath property) {}

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
            output.addAll(process(subpath));
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
    
    private static Collection<Child> process(AccessPath accessPath) {
        if (accessPath instanceof SimpleProperty simpleProperty) {
            return List.of(new Child(simpleProperty.signature(), accessPath));
        }
        else if (accessPath instanceof ComplexProperty complexProperty) {
            return List.of(new Child(complexProperty.signature(), complexProperty));
        }
        
        throw new UnsupportedOperationException("Process");
    }
}
