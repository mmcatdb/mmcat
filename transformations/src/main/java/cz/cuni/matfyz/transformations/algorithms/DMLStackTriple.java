package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class DMLStackTriple {
	
    public final DomainRow pid;
	public final String name;
	public final ComplexProperty t;
	
    public DMLStackTriple(DomainRow pid, String name, ComplexProperty t)
    {
        this.pid = pid;
        this.name = name;
        this.t = t;
    }
    
    @Override
    public String toString()
    {
        var innerBuilder = new IntendedStringBuilder(1);
        innerBuilder.append("pid: ").append(pid).append(",\n");
        innerBuilder.append("name: ").append(name).append(",\n");
        innerBuilder.append("t: ").append(t).append(",\n");
        
        StringBuilder builder = new StringBuilder();
        builder.append("<\n").append(innerBuilder).append(">");
        
        return builder.toString();
    }
}
