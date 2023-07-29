package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.utils.ComparablePair;

import java.util.Set;

/**
 * @author pavel.koupil
 */
public interface AbstractICWrapper {

    void appendIdentifier(String kindName, IdentifierStructure identifier);

    void appendReference(String kindName, String kindName2, Set<ComparablePair<String, String>> attributePairs);

    AbstractStatement createICStatement();

    AbstractStatement createICRemoveStatement();

}
