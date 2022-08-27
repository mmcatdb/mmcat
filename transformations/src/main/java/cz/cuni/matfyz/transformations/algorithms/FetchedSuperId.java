package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.IdWithValues;
import cz.cuni.matfyz.core.record.IComplexRecord;

/**
 * @author jachym.bartik
 */
public class FetchedSuperId {
    
    public final IdWithValues superId;
    public final IComplexRecord childRecord;
    
    public FetchedSuperId(IdWithValues superId, IComplexRecord childRecord) {
        this.superId = superId;
        this.childRecord = childRecord;
    }

    public FetchedSuperId(IdWithValues superId) {
        this(superId, null);
    }
    
}
