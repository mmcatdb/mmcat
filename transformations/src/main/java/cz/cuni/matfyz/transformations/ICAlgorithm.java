package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.utils.ComparablePair;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.statements.ICStatement;

import java.util.*;

import org.javatuples.Pair;

/**
 *
 * @author jachymb.bartik
 */
public class ICAlgorithm
{
    private Mapping mapping;
    private Map<String, Mapping> allMappings;
    private AbstractICWrapper wrapper;
    
    public void input(Mapping mapping, Map<String, Mapping> allMappings, AbstractICWrapper wrapper)
    {
        this.mapping = mapping;
        this.allMappings = allMappings;
        this.wrapper = wrapper;
    }
    
    public ICStatement algorithm()
    {
        throw new UnsupportedOperationException();
        /*
        var N = collectNames(mapping.accessPath(), mapping.primaryIdentifier());
        wrapper.appendIdentifier(mapping.kindName(), N);
        
        for (Pair<String, Set<AccessPath>> referencePair : mapping.references())
        {
            Set<Pair<Signature, Name>> O = collectSigNamePairs(mapping.accessPath(), referencePair.getValue1());
            Mapping n = allMappings.get(referencePair.getValue0());
            Set<Pair<Signature, Name>> R = collectSigNamePairs(n.accessPath(), referencePair.getValue1());
            Set<ComparablePair<String, String>> S = makeReferencePairs(O, R);
            wrapper.appendReference(mapping.kindName(), n.name(), S);
        }
        */
    }
    
    private IdentifierStructure collectNames(AccessPath path, Set<Signature> primaryIdentifier)
    {
        throw new UnsupportedOperationException();
    }

    private Set<Pair<Signature, Name>> collectSigNamePairs(AccessPath path, Set<AccessPath> references)
    {
        throw new UnsupportedOperationException();
    }

    private Set<ComparablePair<String, String>> makeReferencePairs(Set<Pair<Signature, Name>> a, Set<Pair<Signature, Name>> b)
    {
        throw new UnsupportedOperationException();
    }
}
