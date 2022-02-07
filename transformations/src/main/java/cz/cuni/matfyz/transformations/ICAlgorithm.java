package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.utils.ComparablePair;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
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
        IdentifierStructure identifierStructure = collectNames(mapping.accessPath(), mapping.primaryIdentifier());
        wrapper.appendIdentifier(mapping.kindName(), identifierStructure);
        
        for (Reference reference : mapping.references())
        {
            // O
            Map<Signature, Name> referencingAttributes = collectSigNamePairs(mapping.accessPath(), reference.properties());

            // n
            Mapping referencedMapping = allMappings.get(reference.name());

            // R
            Map<Signature, Name> referencedAttributes = collectSigNamePairs(referencedMapping.accessPath(), reference.properties());

            // S
            Set<ComparablePair<String, String>> referencingReferencedNames = makeReferences(referencingAttributes, referencedAttributes);

            wrapper.appendReference(mapping.kindName(), referencedMapping.kindName(), referencingReferencedNames);
        }

        return wrapper.createICRemoveStatement();
    }
    
    private IdentifierStructure collectNames(AccessPath path, IdentifierStructure primaryIdentifier)
    {
        throw new UnsupportedOperationException();
    }

    private Map<Signature, Name> collectSigNamePairs(AccessPath path, Set<AccessPath> referenceProperties)
    {
        var output = new TreeMap<Signature, Name>();

        if (path instanceof ComplexProperty complexPath)
        {
            for (AccessPath referenceProperty : referenceProperties)
            {
                for (AccessPath subpath : complexPath.subpaths())
                {
                    if (referenceProperty.equals(subpath))
                    {
                        output.put(subpath.signature(), subpath.name()); // TODO - nejspíš b se mělo vyhledávat podle něčeho jiného?
                    }
                }
            }
        }

        return output;
    }

    private Set<ComparablePair<String, String>> makeReferences(Map<Signature, Name> a, Map<Signature, Name> b)
    {
        var output = new TreeSet<ComparablePair<String, String>>();

        for (Signature signature : a.keySet())
        {
            String nameA = a.get(signature).toString(); // TODO - toto nefunguje správně - mělo by se použít getStringName(), ale k tomu je potřeba, aby jména byla statická
            String nameB = b.get(signature).toString(); // TODO
            output.add(new ComparablePair<>(nameA, nameB));
        }

        return output;
    }
}
