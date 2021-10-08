/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.util.Pair;
import cz.cuni.matfyz.statements.ICStatement;
import java.util.ArrayList;
import java.util.Set;

interface Constraint
{
    public abstract String getName();
    public abstract String addCommand();
    public abstract String dropCommand();
}

class ReferenceConstraint implements Constraint
{
    private String sourceKindName;
    private String referenceKindName;
    private String sourceAttributeName;
    private String referenceAttributeName;
    
    public String getName()
    {
        return sourceKindName + "#" + sourceAttributeName + "_REFERENCES_" + referenceKindName + "#" + referenceAttributeName;
    }
    
    public ReferenceConstraint(String sourceKindName, String referenceKindName, String sourceAttributeName, String referenceAttributeName)
    {
        this.sourceKindName = sourceKindName;
        this.referenceKindName = referenceKindName;
        this.sourceAttributeName = sourceAttributeName;
        this.referenceAttributeName = referenceAttributeName;
    }
    
    public String addCommand()
    {
        return "ALTER TABLE " + sourceKindName
            + "\nADD CONSTRAINT " + getName()
            + "\nFOREIGN KEY (" + sourceAttributeName + ")"
            + "\nREFERENCES " + referenceKindName + "(" + referenceAttributeName + ");";
    }
    
    public String dropCommand()
    {
        return "ALTER TABLE " + sourceKindName
            + "\nDROP CONSTRAINT " + getName();
    }
}

/**
 *
 * @author jachym.bartik
 */
public class PostgreSQLICWrapper implements AbstractICWrapper
{
    private ArrayList<Constraint> constraints = new ArrayList<Constraint>();
    
	public void appendIdentifier(String kindName, IdentifierStructure identifier)
    {
        // TODO IndentifierStructure is empty
    }

	public void appendReference(String kindName, String kindName2, Set<Pair<String, String>> attributePairs)
    {
        for (Pair<String, String> attributePair : attributePairs)
        {
            // TODO There should be a way how to get attribute names from the pairs
            ReferenceConstraint newConstraint = new ReferenceConstraint(kindName, kindName2, "TODO1", "TODO2");
            
            if (constraints.stream().anyMatch(constraint -> constraint.getName() == newConstraint.getName()))
                return;
            
            constraints.add(newConstraint);
        }
    }

	public ICStatement createICStatement()
    {
        String content = String.join("\n\n", constraints.stream().map(constraint -> constraint.addCommand()).toList());
        return new PostgreSQLICStatement(content);
    }

	public ICStatement createICRemoveStatement()
    {
        String content = String.join("\n\n", constraints.stream().map(constraint -> constraint.dropCommand()).toList());
        return new PostgreSQLICStatement(content);
    }
}

class PostgreSQLICStatement implements ICStatement
{
    private String content;
    
    public PostgreSQLICStatement(String content) {
        this.content = content;
    }
}
