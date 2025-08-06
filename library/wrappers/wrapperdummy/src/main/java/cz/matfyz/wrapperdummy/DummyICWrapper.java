package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DummyICWrapper implements AbstractICWrapper {

    @Override public void clear() {
        methods.add("clear()");
    }

    private List<String> methods = new ArrayList<>();

    public List<String> methods() {
        return methods;
    }

    @Override public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        methods.add("appendIdentifier(" + kindName + ", [ " + identifier + " ])");
    }

    @Override public void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
        methods.add("appendReference(" + referencingKind + ", " + referencedKind + ", " +  attributePairsToString(attributePairs) + ")");
    }

    @Override public Collection<AbstractStatement> createICStatements() {
        methods.add("createICStatements()");
        return List.of();
    }

    @Override public Collection<AbstractStatement> dropICStatements() {
        methods.add("dropICStatements()");
        return List.of();
    }

    private String attributePairsToString(Set<AttributePair> pairs) {
        final var sb = new StringBuilder();

        sb.append("[");
        int index = 0;
        for (var pair : pairs) {
            if (index > 0)
                sb.append(",");
            index++;
            sb.append(" ").append("(").append(pair.referencing()).append(", ").append(pair.referenced()).append(")");
        }

        if (index > 0)
            sb.append(" ");
        sb.append("]");

        return sb.toString();
    }
}
