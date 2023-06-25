package cz.cuni.matfyz.wrapperneo4j;

import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractStatement;
import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.utils.ComparablePair;

import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class Neo4jICWrapper implements AbstractICWrapper {
    
    @Override
    public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        // Intentionally empty
    }

    @Override
    public void appendReference(String kindName, String kindName2, Set<ComparablePair<String, String>> attributePairs) {
        // Intentionally empty
    }

    @Override
    public AbstractStatement createICStatement() {
        return Neo4jStatement.createEmpty();
    }

    @Override
    public AbstractStatement createICRemoveStatement() {
        return Neo4jStatement.createEmpty();
    }
}