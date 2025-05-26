package cz.matfyz.abstractwrappers;

import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AbstractICWrapper {

    /**
     * Prepares the wrapper for the next kind. Very important - don't underestimate!
     */
    void clear();

    void appendIdentifier(String kindName, IdentifierStructure identifier);

    record AttributePair(String referencing, String referenced) implements Comparable<AttributePair> {

        @Override public int compareTo(AttributePair pair) {
            int firstResult = referencing.compareTo(pair.referencing);
            return firstResult != 0 ? firstResult : referenced.compareTo(pair.referenced);
        }

    }

    void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs);

    Collection<AbstractStatement> createICStatements();

    Collection<AbstractStatement> dropICStatements();

    static AbstractICWrapper createEmpty() {
        return EmptyICWrapper.instance;
    }

    class EmptyICWrapper implements AbstractICWrapper {

        private EmptyICWrapper() {}

        private static final EmptyICWrapper instance = new EmptyICWrapper();

        @Override public void clear() {
            // This method intentionally does nothing.
        }

        @Override public void appendIdentifier(String kindName, IdentifierStructure identifier) {
            // This method intentionally does nothing.
        }

        @Override public void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
            // This method intentionally does nothing.
        }

        @Override public Collection<AbstractStatement> createICStatements() {
            return List.of();
        }

        @Override public Collection<AbstractStatement> dropICStatements() {
            return List.of();
        }

    }

}
