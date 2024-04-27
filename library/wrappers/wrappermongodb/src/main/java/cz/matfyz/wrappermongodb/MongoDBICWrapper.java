package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.Set;

public class MongoDBICWrapper implements AbstractICWrapper {

    @Override public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        // Intentionally empty
    }

    @Override public void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
        // Intentionally empty
    }

    @Override public AbstractStatement createICStatement() {
        return MongoDBEmptyStatement.getInstance();
    }

    @Override public AbstractStatement createICRemoveStatement() {
        return MongoDBEmptyStatement.getInstance();
    }

}
