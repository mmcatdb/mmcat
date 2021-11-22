package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class SuperIdWithValues
{
    private final Map<Signature, String> map;
    
    public Collection<Signature> signatures()
    {
        return map.keySet();
    }
    
    public Collection<String> values()
    {
        return map.values();
    }
    
    public Map<Signature, String> map()
    {
        return map;
    }

    private SuperIdWithValues(Map<Signature, String> map)
    {
		this.map = map;
	}
    
    public static class Builder
    {
        private Map<Signature, String> map = new TreeMap<>();

        public void add(Signature signature, String value)
        {
            map.put(signature, value);
        }

        public SuperIdWithValues build()
        {
            var output = new SuperIdWithValues(map);
            map = new TreeMap<>();
            return output;
        }
    }
    
    /*
    public Match compareToActiveDomainRow(ActiveDomainRow row)
    {
        int numberOfNulls = 0;
        for (Signature signature : signatures())
        {
            String value = map.get(signature);
            String rowValue = row.tuples().get(signature);
            
            if (rowValue == null)
                numberOfNulls++;
            else if (value != rowValue)
                return Match.NONE;
        }
        
        int sizeDifference = map.size() - row.tuples().size();
        
        if (sizeDifference == 0)
            return numberOfNulls == 0 ? Match.EXACT : Match.UNION;
        else if (sizeDifference > 0)
            return numberOfNulls == sizeDifference ? Match.SUPERSET : Match.UNION;
        else
            return numberOfNulls == 0 ? Match.SUBSET : Match.UNION;
    }
    */

    // TODO - this is only auto-generated code.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SuperIdWithValues other = (SuperIdWithValues) obj;
        if (!Objects.equals(this.map, other.map)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.map);
        return hash;
    }
}

/*
public enum Match
{
    EXACT,
    SUBSET,
    SUPERSET,
    UNION, // All values for the same signatures are equal, but both sets have some different signatures.
    NONE // At least one signature leads to different (non-null) values in both sets.
}
*/