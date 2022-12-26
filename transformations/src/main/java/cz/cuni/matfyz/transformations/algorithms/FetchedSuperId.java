package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.core.record.IComplexRecord;

/**
 * @author jachym.bartik
 */
public class FetchedSuperId {
    
    public final SuperIdWithValues superId;
    public final IComplexRecord childRecord;
    
    public FetchedSuperId(SuperIdWithValues superId, IComplexRecord childRecord) {
        this.superId = superId;
        this.childRecord = childRecord;
    }

    public FetchedSuperId(SuperIdWithValues superId) {
        this(superId, null);
    }
    
}
