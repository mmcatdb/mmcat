package cz.matfyz.abstractwrappers;

import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.Set;

public interface AbstractICWrapper {

    void appendIdentifier(String kindName, IdentifierStructure identifier);

    public record AttributePair(String referencing, String referenced) implements Comparable<AttributePair> {

        @Override public int compareTo(AttributePair object) {
            int firstResult = referencing.compareTo(object.referencing);
            return firstResult != 0 ? firstResult : referenced.compareTo(object.referenced);
        }

    }

    void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs);

    AbstractStatement createICStatement();

    AbstractStatement createICRemoveStatement();

    public static AbstractICWrapper createEmpty() {
        return EmptyICWrapper.instance;
    }

    class EmptyICWrapper implements AbstractICWrapper {

        private EmptyICWrapper() {}

        private static final EmptyICWrapper instance = new EmptyICWrapper();

        @Override
        public void appendIdentifier(String kindName, IdentifierStructure identifier) {
            // This method intentionally does nothing.
        }
    
        @Override
        public void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
            // This method intentionally does nothing.
        }
    
        @Override
        public AbstractStatement createICStatement() {
            return AbstractStatement.createEmpty();
        }
    
        @Override
        public AbstractStatement createICRemoveStatement() {
            return AbstractStatement.createEmpty();
        }
    
    }
    
}
