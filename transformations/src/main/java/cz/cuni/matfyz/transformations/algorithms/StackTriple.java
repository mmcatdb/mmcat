package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.*;
import cz.cuni.matfyz.core.utils.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class StackTriple {
	
	public final SchemaMorphism mS;
	public final ActiveDomainRow pid;
	public final AccessPath t;
    public final IComplexRecord record;
	
/*
	public StackTriple(IdWithValues sid_dom, Object context, Object value)
    {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
*/
    
    public StackTriple(ActiveDomainRow pid, SchemaMorphism mS, AccessPath t, IComplexRecord record)
    {
        this.pid = pid;
        this.mS = mS;
        this.t = t;
        this.record = record;
    }
    
    @Override
    public String toString()
    {
        var innerBuilder = new IntendedStringBuilder(1);
        innerBuilder.append("mS: ").append(mS.signature()).append(",\n");
        innerBuilder.append("pid: ").append(pid).append(",\n");
        innerBuilder.append("t: ").append(t).append(",\n");
        innerBuilder.append("record: ").append(record);
        
        StringBuilder builder = new StringBuilder();
        builder.append("<\n").append(innerBuilder).append(">");
        
        return builder.toString();
    }
}
