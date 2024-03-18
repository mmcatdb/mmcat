package cz.matfyz.abstractwrappers;

import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.Set;

/**
 * @author pavel.koupil
 */
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

}
