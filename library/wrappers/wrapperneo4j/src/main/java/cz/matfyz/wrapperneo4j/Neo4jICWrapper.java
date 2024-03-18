package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class Neo4jICWrapper implements AbstractICWrapper {

    @Override public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        // Intentionally empty
    }

    @Override public void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
        // Intentionally empty
    }

    @Override public AbstractStatement createICStatement() {
        return Neo4jStatement.createEmpty();
    }

    @Override public AbstractStatement createICRemoveStatement() {
        return Neo4jStatement.createEmpty();
    }

}
