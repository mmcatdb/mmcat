package cz.cuni.matfyz.core.instance;

/**
 * This class represents a relation between two members of two active domains ({@link ActiveDomainRow}).
 * It corresponds to a single {@link InstanceMorphism}.
 * @author jachym.bartik
 */
public class ActiveMappingRow implements Comparable<ActiveMappingRow>
{
	private final ActiveDomainRow domainRow;
    private final ActiveDomainRow codomainRow;
    
    public ActiveDomainRow domainRow()
    {
        return domainRow;
    }
    
    public ActiveDomainRow codomainRow()
    {
        return codomainRow;
    }
    
    public ActiveMappingRow(ActiveDomainRow domainRow, ActiveDomainRow codomainRow)
    {
        this.domainRow = domainRow;
        this.codomainRow = codomainRow;
    }

    @Override
    public int compareTo(ActiveMappingRow row)
    {
        int domainCompareResult = domainRow.compareTo(row.domainRow);
        return domainCompareResult != 0 ? domainCompareResult : codomainRow.compareTo(row.codomainRow);
    }
    
    @Override
	public String toString()
    {
		StringBuilder builder = new StringBuilder();

        builder.append(domainRow).append(" -> ").append(codomainRow);
        
        return builder.toString();
	}
    
    @Override
    public boolean equals(Object object)
    {
        return object instanceof ActiveMappingRow activeMappingRow ? equals(activeMappingRow) : false;
    }
    
    public boolean equals(ActiveMappingRow row)
    {
        if (row == null)
            return false;
        
        return domainRow.equals(row.domainRow) && codomainRow.equals(row.codomainRow);
    }
}
