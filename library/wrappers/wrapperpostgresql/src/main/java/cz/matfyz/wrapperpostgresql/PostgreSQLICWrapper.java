package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.core.mapping.IdentifierStructure;
import cz.matfyz.core.utils.ComparablePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLICWrapper implements AbstractICWrapper {

    private final List<Constraint> constraints = new ArrayList<>();
    
    @Override public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        constraints.add(new IdentifierConstraint(kindName, identifier.properties()));
    }

    @Override public void appendReference(String referencingKindName, String referencedKindName, Set<ComparablePair<String, String>> attributePairs) {
        constraints.add(new ReferenceConstraint(referencingKindName, referencedKindName, attributePairs));
    }

    @Override public PostgreSQLStatement createICStatement() {
        String content = "\n" + String.join("\n\n", constraints.stream().map(Constraint::addCommand).toList()) + "\n";
        return new PostgreSQLStatement(content);
    }

    @Override public PostgreSQLStatement createICRemoveStatement() {
        String content = "\n" + String.join("\n\n", constraints.stream().map(Constraint::dropCommand).toList()) + "\n";
        return new PostgreSQLStatement(content);
    }

}

interface Constraint {

    public abstract String addCommand();

    public abstract String dropCommand();

}

class IdentifierConstraint implements Constraint {
    
    private String kindName;
    private Collection<String> properties;

    public IdentifierConstraint(String kindName, Collection<String> properties) {
        this.kindName = kindName;
        this.properties = properties;
    }

    private String getName() {
        return "#" + kindName + "_PRIMARY_KEY";
    }

    @Override public String addCommand() {
        return "ALTER TABLE \"" + kindName + "\""
            + "\nADD CONSTRAINT \"" + getName() + "\""
            + "\nPRIMARY KEY (\"" + String.join("\", \"", properties) + "\")" + ";";
    }

    @Override public String dropCommand() {
        return "\nALTER TABLE \"" + kindName + "\""
            + "\nDROP CONSTRAINT \"" + getName() + "\";";
    }

}

class ReferenceConstraint implements Constraint {
    
    private String referencingKindName;
    private String referencedKindName;
    private List<String> referencingAttributes;
    private List<String> referencedAttributes;

    public ReferenceConstraint(String referencingKindName, String referencedKindName, Set<ComparablePair<String, String>> attributePairs) {
        this.referencingKindName = referencingKindName;
        this.referencedKindName = referencedKindName;
        this.referencingAttributes = attributePairs.stream().map(ComparablePair::getValue1).toList();
        this.referencedAttributes = attributePairs.stream().map(ComparablePair::getValue2).toList();
    }
    
    private String getName() {
        return "#" + referencingKindName + "_REFERENCES_" + referencedKindName;
    }

    @Override public String addCommand() {
        return "ALTER TABLE \"" + referencingKindName + "\""
            + "\nADD CONSTRAINT \"" + getName() + "\""
            + "\nFOREIGN KEY (\"" + String.join("\", \"", referencingAttributes) + "\")"
            + "\nREFERENCES \"" + referencedKindName + "\" (\"" + String.join("\", \"", referencedAttributes) + "\");";
    }
    
    @Override public String dropCommand() {
        return "ALTER TABLE \"" + referencingKindName + "\""
            + "\nDROP CONSTRAINT \"" + getName() + "\";";
    }

}