package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * This class represents an inner node (so it can be root) of the record tree.
 * The value of this record are its children.
 * @author jachymb.bartik
 */
public interface IComplexRecord
{
    public RecordName name();

    public boolean hasComplexRecords(Signature signature);

    public List<? extends IComplexRecord> getComplexRecords(Signature signature);
    
    public boolean hasDynamicNameChildren();
    
    public List<? extends IComplexRecord> getDynamicNameChildren();

    public Signature dynamicNameSignature();

    public boolean hasSimpleRecord(Signature signature);

    public SimpleRecord<?> getSimpleRecord(Signature signature);

    public boolean hasDynamicNameValues();
    
    public List<SimpleValueRecord<?>> getDynamicNameValues();
    
    public boolean containsDynamicNameValue(Signature signature);
}
