package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.utils.ComparablePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLICWrapper implements AbstractICWrapper {

    private final List<Constraint> constraints = new ArrayList<>();
    
    @Override
    public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        constraints.add(new IdentifierConstraint(kindName, identifier.properties()));
    }

    @Override
    public void appendReference(String kindName, String kindName2, Set<ComparablePair<String, String>> attributePairs) {
        //throw new UnsupportedOperationException();
        /*
        for (ComparablePair<String, String> attributePair : attributePairs) {

            // TODO There should be a way how to get attribute names from the pairs.
            ReferenceConstraint newConstraint = new ReferenceConstraint(kindName, kindName2, "TODO1", "TODO2");
            
            if (constraints.stream().anyMatch(constraint -> constraint.getName().equals(newConstraint.getName())))
                return;
            
            constraints.add(newConstraint);
        }
        */
    }

    @Override
    public PostgreSQLICStatement createICStatement() {
        String content = "\n" + String.join("\n\n", constraints.stream().map(Constraint::addCommand).toList()) + "\n";
        return new PostgreSQLICStatement(content);
    }

    @Override
    public PostgreSQLICStatement createICRemoveStatement() {
        String content = "\n" + String.join("\n\n", constraints.stream().map(Constraint::dropCommand).toList()) + "\n";
        return new PostgreSQLICStatement(content);
    }

}

interface Constraint {

    public abstract String addCommand();

    public abstract String dropCommand();

}

class IdentifierConstraint implements Constraint {
    
    private String sourceKindName;
    private Collection<String> properties;

    public IdentifierConstraint(String sourceKindName, Collection<String> properties) {
        this.sourceKindName = sourceKindName;
        this.properties = properties;
    }

    private String getName() {
        return sourceKindName + "_PRIMARY_KEY_" + String.join("#", properties);
    }

    @Override
    public String addCommand() {
        return "ALTER TABLE " + sourceKindName
            + "\nADD CONSTRAINT " + getName()
            + "\nPRIMARY KEY (" + String.join(", ", properties) + ")" + ";";
    }

    @Override
    public String dropCommand() {
        return "\nALTER TABLE " + sourceKindName
            + "\nDROP CONSTRAINT " + getName() + ";";
    }

}

class ReferenceConstraint implements Constraint {
    
    private String sourceKindName;
    private String referenceKindName;
    private String sourceAttributeName;
    private String referenceAttributeName;

    public ReferenceConstraint(String sourceKindName, String referenceKindName, String sourceAttributeName, String referenceAttributeName) {
        this.sourceKindName = sourceKindName;
        this.referenceKindName = referenceKindName;
        this.sourceAttributeName = sourceAttributeName;
        this.referenceAttributeName = referenceAttributeName;
    }
    
    private String getName() {
        return sourceKindName + "#" + sourceAttributeName + "_REFERENCES_" + referenceKindName + "#" + referenceAttributeName;
    }
    
    @Override
    public String addCommand() {
        return "ALTER TABLE " + sourceKindName
            + "\nADD CONSTRAINT " + getName()
            + "\nFOREIGN KEY (" + sourceAttributeName + ")"
            + "\nREFERENCES " + referenceKindName + "(" + referenceAttributeName + ");";
    }
    
    @Override
    public String dropCommand() {
        return "ALTER TABLE " + sourceKindName
            + "\nDROP CONSTRAINT " + getName() + ";";
    }

}