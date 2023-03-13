package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.utils.ComparablePair;
import cz.cuni.matfyz.statements.AbstractStatement;
import cz.cuni.matfyz.transformations.exception.TransformationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author jachymb.bartik
 */
public class ICAlgorithm {
    
    private Mapping mapping;
    private Map<SchemaObject, Mapping> mappingsByObjects;
    private AbstractICWrapper wrapper;

    private Map<String, Set<ComparablePair<String, String>>> referencesForAllKinds = new TreeMap<>();
    
    public void input(Mapping mapping, Iterable<Mapping> allMappings, AbstractICWrapper wrapper) {
        this.mapping = mapping;
        this.wrapper = wrapper;
        this.mappingsByObjects = new TreeMap<>();
        allMappings.forEach(m -> mappingsByObjects.put(m.rootObject(), m));
    }
    
    public AbstractStatement algorithm() {
        // Primary key constraint
        IdentifierStructure identifierStructure = collectNames(mapping.accessPath(), mapping.primaryKey());
        wrapper.appendIdentifier(mapping.kindName(), identifierStructure);
        
        // Reference keys constraints
        processPath(mapping.accessPath(), mapping, Signature.createEmpty());
        referencesForAllKinds.forEach((referencedKindName, references) -> wrapper.appendReference(mapping.kindName(), referencedKindName, references));

        return wrapper.createICStatement();

        /*
        for (Reference reference : mapping.references()) {
            // O
            Map<Signature, String> referencingAttributes = collectSignatureNamePairs(mapping.accessPath(), reference.properties());
            // n
            Mapping referencedMapping = allMappings.get(reference.name());
            // R
            Map<Signature, String> referencedAttributes = collectSignatureNamePairs(referencedMapping.accessPath(), reference.properties());
            // S
            Set<ComparablePair<String, String>> referencingReferencedNames = makeReferences(referencingAttributes, referencedAttributes);
            wrapper.appendReference(mapping.kindName(), referencedMapping.kindName(), referencingReferencedNames);
        }
        */
    }

    /**
     * For each signature from primaryKey, we look to the access path to find a subpath with given signature.
     * The set of these names is then returned.
     * @param path Access path corresponding to the kind of the mapping.
     * @param primaryKey An eventually ordered collection of signatures of morphisms whose codomains correspond to properties forming the primary identifier of given kind.
     * @return The set of names corresponding to signatures from primaryKey.
     */
    private IdentifierStructure collectNames(ComplexProperty path, Collection<Signature> primaryKey) {
        Collection<String> output = new ArrayList<>();

        for (Signature signature : primaryKey) {
            final var subpath = path.getSubpathBySignature(signature);
            if (subpath == null)
                continue;

            if (subpath.name() instanceof StaticName staticName)
                output.add(staticName.getStringName());
            else
                // These names are identifiers of given kind so they must be unique among all names.
                // This quality can't be achieved by dynamic names so they aren't supported here.
                throw new TransformationException("Collect names.");
        }
        
        return new IdentifierStructure(output);
    }

    private void processPath(AccessPath path, Mapping lastMapping, Signature signatureFromLastMapping) {
        //final var newSignatureFromLastMapping = signatureFromLastMapping.concatenate(path.signature());

        for (final var baseSignature : path.signature().toBasesReverse()) {
            final var object = mapping.category().getMorphism(baseSignature).cod();
            final var objectMapping = mappingsByObjects.get(object);

            if (objectMapping == null) {
                signatureFromLastMapping = signatureFromLastMapping.concatenate(baseSignature);
            }
            else {
                lastMapping = objectMapping;
                signatureFromLastMapping = Signature.createEmpty();
            }
        }

        if (path instanceof SimpleProperty simpleProperty)
            processSimpleSubpath(simpleProperty, lastMapping, signatureFromLastMapping);
        else if (path instanceof ComplexProperty complexProperty)
            processAllSubpaths(complexProperty, lastMapping, signatureFromLastMapping);
    }

    private void processSimpleSubpath(SimpleProperty property, Mapping lastMapping, Signature signatureFromLastMapping) {
        if (lastMapping.equals(mapping))
            return;

        if (!lastMapping.primaryKey().contains(signatureFromLastMapping))
            return;

        // The property is contained in the primary key of the mapping meaning we have to include it in the reference keys.
        final var subpathInLastMapping = lastMapping.accessPath().getSubpathBySignature(signatureFromLastMapping);
        if (!(subpathInLastMapping instanceof SimpleProperty referencedProperty))
            return;

        if (!(property.name() instanceof StaticName referencingName))
            return;

        if (!(referencedProperty.name() instanceof StaticName referencedName))
            return;

        final var referencesForKind = referencesForAllKinds.computeIfAbsent(lastMapping.kindName(), x -> new TreeSet<>());
        referencesForKind.add(new ComparablePair<String,String>(referencingName.toString(), referencedName.toString()));
    }
/*
    private void processComplexSubpath(ComplexProperty property, Mapping lastMapping, Signature signatureFromLastMapping) {
        if (property.isAuxiliary()) {
            processAllSubpaths(property, lastMapping, signatureFromLastMapping);
            return;
        }

        // There is a schema object corresponding to the property.
        final var pathObject = mapping.category().getMorphism(property.signature()).cod();
        final var pathMapping = mappingsByObjects.get(pathObject);
        if (pathMapping == null) {
            processAllSubpaths(property, lastMapping, signatureFromLastMapping);
            return;
        }

        // There is a mapping corresponding to the object.
        // We take the mapping as a new potential holder of the referenced properties and we start building new signature from it towards the simple properties in order to compare it with the primary keys of this mapping.
        processAllSubpaths(property, pathMapping, Signature.createEmpty());
    }
 */
    private void processAllSubpaths(ComplexProperty complexProperty, Mapping lastMapping, Signature signatureFromLastMapping) {
        complexProperty.subpaths().forEach(subpath -> processPath(subpath, lastMapping, signatureFromLastMapping));
    }

    /**
     * @param path
     * @param referenceProperties A set of signatures.
     * @return
     */
    /*
    private Map<Signature, String> collectSignatureNamePairs(ComplexProperty path, Set<Signature> referenceProperties) {
        var output = new TreeMap<Signature, String>();

        for (Signature signature : referenceProperties) {
            if (path.getSubpathBySignature(signature).name() instanceof StaticName staticName)
                output.put(signature, staticName.getStringName());
            else
                throw new TransformationException();
        }

        return output;
    }

    private Set<ComparablePair<String, String>> makeReferences(Map<Signature, String> a, Map<Signature, String> b) {
        var output = new TreeSet<ComparablePair<String, String>>();

        for (var entry : a.entrySet()) {
            String nameA = entry.getValue();
            String nameB = b.get(entry.getKey());
            output.add(new ComparablePair<>(nameA, nameB));
        }

        return output;
    }
    */

}
