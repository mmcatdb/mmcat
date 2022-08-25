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
	
	public final SchemaMorphism parentToChildMorphism;
	public final DomainRow parentRow;
	public final AccessPath t;
    public final IComplexRecord parentRecord;
	
/*
	public StackTriple(IdWithValues sid_dom, Object context, Object value)
    {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
*/
    
    public StackTriple(DomainRow pid, SchemaMorphism mS, AccessPath t, IComplexRecord record)
    {
        this.parentRow = pid;
        this.parentToChildMorphism = mS;
        this.t = t;
        this.parentRecord = record;
    }
    
    @Override
    public String toString()
    {
        var innerBuilder = new IntendedStringBuilder(1);
        innerBuilder.append("mS: ").append(parentToChildMorphism.signature()).append(",\n");
        innerBuilder.append("pid: ").append(parentRow).append(",\n");
        innerBuilder.append("t: ").append(t).append(",\n");
        innerBuilder.append("record: ").append(parentRecord);
        
        StringBuilder builder = new StringBuilder();
        builder.append("<\n").append(innerBuilder).append(">");
        
        return builder.toString();
    }
}
