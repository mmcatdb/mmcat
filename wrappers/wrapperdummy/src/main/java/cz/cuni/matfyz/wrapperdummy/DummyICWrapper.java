package cz.cuni.matfyz.wrapperdummy;

import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.utils.ComparablePair;
import cz.cuni.matfyz.statements.ICStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author jachym.bartik
 */
public class DummyICWrapper implements AbstractICWrapper {

    private List<String> methods = new ArrayList<>();

    public List<String> methods() {
        return methods;
    }
    
    @Override
    public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        methods.add("appendIdentifier(" + kindName + ", [ " + identifier + " ])");
    }

    @Override
    public void appendReference(String kindName, String kindName2, Set<ComparablePair<String, String>> attributePairs) {
        methods.add("appendReference(" + kindName + ", " + kindName2 + ", " +  attributePairsToString(attributePairs) + ")");
    }

    @Override
    public ICStatement createICRemoveStatement() {
        methods.add("createICRemoveStatement()");
        return new DummyICStatement("");
    }

    @Override
    public ICStatement createICStatement() {
        methods.add("createICStatement()");

        return new DummyICStatement("");
    }

    private String attributePairsToString(Set<ComparablePair<String, String>> pairs) {
        var builder = new StringBuilder();

        builder.append("[");
        int index = 0;
        for (var pair : pairs) {
            if (index > 0)
                builder.append(",");
            index++;
            builder.append(" ").append("(").append(pair.getValue1()).append(", ").append(pair.getValue2()).append(")");
        }

        if (index > 0)
            builder.append(" ");
        builder.append("]");

        return builder.toString();
    }
}
