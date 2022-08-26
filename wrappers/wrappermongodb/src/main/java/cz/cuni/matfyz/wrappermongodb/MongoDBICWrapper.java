package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.utils.ComparablePair;

import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class MongoDBICWrapper implements AbstractICWrapper {
    
    @Override
    public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendReference(String kindName, String kindName2, Set<ComparablePair<String, String>> attributePairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MongoDBICStatement createICStatement() {
        return new MongoDBICStatement("");
    }

    @Override
    public MongoDBICStatement createICRemoveStatement() {
        return new MongoDBICStatement("");
    }
}