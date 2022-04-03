package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.ComparablePair;
import cz.cuni.matfyz.abstractWrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.statements.ICStatement;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class ICAlgorithm
{
    private Mapping mapping;
    private Map<Name, Mapping> allMappings;
    private AbstractICWrapper wrapper;
    
    public void input(Mapping mapping, Map<Name, Mapping> allMappings, AbstractICWrapper wrapper)
    {
        this.mapping = mapping;
        this.allMappings = allMappings;
        this.wrapper = wrapper;
    }
    
    public ICStatement algorithm()
    {
        // N
        IdentifierStructure identifierStructure = collectNames(mapping.accessPath(), mapping.pkey());
        wrapper.appendIdentifier(mapping.kindName(), identifierStructure);
        
        for (Reference reference : mapping.references())
        {
            // O
            Map<Signature, String> referencingAttributes = collectSigNamePairs(mapping.accessPath(), reference.properties());

            // n
            Mapping referencedMapping = allMappings.get(reference.name());

            // R
            Map<Signature, String> referencedAttributes = collectSigNamePairs(referencedMapping.accessPath(), reference.properties());

            // S
            Set<ComparablePair<String, String>> referencingReferencedNames = makeReferences(referencingAttributes, referencedAttributes);

            wrapper.appendReference(mapping.kindName(), referencedMapping.kindName(), referencingReferencedNames);
        }

        return wrapper.createICRemoveStatement();
    }

    /**
     * For each signature from pkey, we look to the access path to find a subpath with given signature.
     * The set of these names is then returned.
     * @param path Access path corresponding to the kind of the mapping.
     * @param pkey An eventually ordered collection of signatures of morphisms whose codomains correspond to properties forming the primary identifier of given kind.
     * @return The set of names corresponding to signatures from pkey.
     */
    private IdentifierStructure collectNames(ComplexProperty path, Collection<Signature> pkey)
    {
        Collection<String> output = new ArrayList<>();

        for (Signature signature : pkey)
        {
            if (path.getSubpathBySignature(signature).name() instanceof StaticName staticName)
                output.add(staticName.getStringName());
            else
                // These names are identifiers of given kind so they must be unique among all names.
                // This quality can't be achieved by dynamic names so they aren't supported here.
                throw new UnsupportedOperationException();
        }
        
        return new IdentifierStructure(output);
    }

    /**
     * 
     * @param path
     * @param referenceProperties A set of signatures.
     * @return
     */
    private Map<Signature, String> collectSigNamePairs(ComplexProperty path, Set<Signature> referenceProperties)
    {
        var output = new TreeMap<Signature, String>();

        for (Signature signature : referenceProperties)
        {
            if (path.getSubpathBySignature(signature).name() instanceof StaticName staticName)
                output.put(signature, staticName.getStringName());
            else
                throw new UnsupportedOperationException();
        }

        return output;
    }

    private Set<ComparablePair<String, String>> makeReferences(Map<Signature, String> a, Map<Signature, String> b)
    {
        var output = new TreeSet<ComparablePair<String, String>>();

        for (Signature signature : a.keySet())
        {
            String nameA = a.get(signature);
            String nameB = b.get(signature);
            output.add(new ComparablePair<>(nameA, nameB));
        }

        return output;
    }
}
