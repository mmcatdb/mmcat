package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractWrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.utils.ComparablePair;
import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLICWrapper implements AbstractICWrapper
{
    private final List<Constraint> constraints = new ArrayList<>();
    
    @Override
	public void appendIdentifier(String kindName, IdentifierStructure identifier)
    {
        // TODO IndentifierStructure is empty.
    }

    @Override
	public void appendReference(String kindName, String kindName2, Set<ComparablePair<String, String>> attributePairs)
    {
        for (ComparablePair<String, String> attributePair : attributePairs)
        {
            // TODO There should be a way how to get attribute names from the pairs.
            ReferenceConstraint newConstraint = new ReferenceConstraint(kindName, kindName2, "TODO1", "TODO2");
            
            if (constraints.stream().anyMatch(constraint -> constraint.getName().equals(newConstraint.getName())))
                return;
            
            constraints.add(newConstraint);
        }
    }

    @Override
	public PostgreSQLICStatement createICStatement()
    {
        String content = String.join("\n\n", constraints.stream().map(constraint -> constraint.addCommand()).toList());
        return new PostgreSQLICStatement(content);
    }

    @Override
	public PostgreSQLICStatement createICRemoveStatement()
    {
        String content = String.join("\n\n", constraints.stream().map(constraint -> constraint.dropCommand()).toList());
        return new PostgreSQLICStatement(content);
    }
}

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