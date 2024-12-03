package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper.AttributePair;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.core.mapping.IdentifierStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PostgreSQLICWrapper implements AbstractICWrapper {

    @Override public void clear() {
        constraints.clear();
    }

    private final List<Constraint> constraints = new ArrayList<>();

    @Override public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        constraints.add(new IdentifierConstraint(kindName, identifier.properties()));
    }

    @Override public void appendReference(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
        constraints.add(new ReferenceConstraint(referencingKind, referencedKind, attributePairs));
    }

    @Override public StringStatement createICStatement() {
        String content = "\n" + String.join("\n\n", constraints.stream().map(Constraint::addCommand).toList()) + "\n";
        return new StringStatement(content);
    }

    @Override public StringStatement createICRemoveStatement() {
        String content = "\n" + String.join("\n\n", constraints.stream().map(Constraint::dropCommand).toList()) + "\n";
        return new StringStatement(content);
    }

}

interface Constraint {

    String addCommand();

    String dropCommand();

}

class IdentifierConstraint implements Constraint {

    private String kindName;
    private Collection<String> properties;

    IdentifierConstraint(String kindName, Collection<String> properties) {
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

    private String referencingKind;
    private String referencedKind;
    private List<String> referencingAttributes;
    private List<String> referencedAttributes;

    ReferenceConstraint(String referencingKind, String referencedKind, Set<AttributePair> attributePairs) {
        this.referencingKind = referencingKind;
        this.referencedKind = referencedKind;
        this.referencingAttributes = attributePairs.stream().map(AttributePair::referencing).toList();
        this.referencedAttributes = attributePairs.stream().map(AttributePair::referenced).toList();
    }

    private String getName() {
        return "#" + referencingKind + "_REFERENCES_" + referencedKind;
    }

    @Override public String addCommand() {
        return "ALTER TABLE \"" + referencingKind + "\""
            + "\nADD CONSTRAINT \"" + getName() + "\""
            + "\nFOREIGN KEY (\"" + String.join("\", \"", referencingAttributes) + "\")"
            + "\nREFERENCES \"" + referencedKind + "\" (\"" + String.join("\", \"", referencedAttributes) + "\");";
    }

    @Override public String dropCommand() {
        return "ALTER TABLE \"" + referencingKind + "\""
            + "\nDROP CONSTRAINT \"" + getName() + "\";";
    }

}
