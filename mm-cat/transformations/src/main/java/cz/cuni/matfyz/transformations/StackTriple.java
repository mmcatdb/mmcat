package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.record.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class StackTriple {
	
	public final SchemaMorphism mS;
	public final ActiveDomainRow pid;
	public final AccessPath t;
    public final ComplexRecord record;
	
/*
	public StackTriple(IdWithValues sid_dom, Object context, Object value)
    {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
*/
    
    public StackTriple(ActiveDomainRow pid, SchemaMorphism mS, AccessPath t, ComplexRecord record)
    {
        this.pid = pid;
        this.mS = mS;
        this.t = t;
        this.record = record;
    }
}
