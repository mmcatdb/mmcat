package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.mapping.IdentifierStructure;
import cz.matfyz.core.utils.ComparablePair;

import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class MongoDBICWrapper implements AbstractICWrapper {
    
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
        return MongoDBEmptyStatement.getInstance();
    }

    @Override
    public AbstractStatement createICRemoveStatement() {
        return MongoDBEmptyStatement.getInstance();
    }
}