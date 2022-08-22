package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.IdWithValues;
import cz.cuni.matfyz.core.record.*;

/**
 *
 * @author jachym.bartik
 */
public class FetchedSuperId {
	
	public final IdWithValues idWithValues;
	public final IComplexRecord childRecord;
    
    public FetchedSuperId(IdWithValues idWithValues, IComplexRecord childRecord) {
        this.idWithValues = idWithValues;
        this.childRecord = childRecord;
    }

    public FetchedSuperId(IdWithValues idWithValues) {
        this(idWithValues, null);
    }
    
}
