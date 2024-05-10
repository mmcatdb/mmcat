package cz.matfyz.abstractwrappers.utils;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.Set;

// TODO: maybe rename to do-nothing-IC wrapper, or something like that
public class GenericICWrapper implements AbstractICWrapper {

    @Override
    public void appendIdentifier(String kindName, IdentifierStructure identifier) {
    }

    @Override
    public void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
    }

    @Override
    public AbstractStatement createICStatement() {
        return EmptyStatement.getInstance();
    }

    @Override
    public AbstractStatement createICRemoveStatement() {
        return EmptyStatement.getInstance();
    }

    private static class EmptyStatement implements AbstractStatement {
        
        private EmptyStatement() {}
        
        @Override
        public String getContent() {
            return ""; 
        }

        private static EmptyStatement instance = new EmptyStatement();

        private static EmptyStatement getInstance() {
            return instance;
        }
    }
}
