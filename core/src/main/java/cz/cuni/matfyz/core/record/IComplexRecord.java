package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * This class represents an inner node of the record tree.
 * The value of this record are its children.
 * @author jachymb.bartik
 */
public interface IComplexRecord
{
    public RecordName name();

    public boolean hasComplexRecords(Signature signature);

    public List<? extends IComplexRecord> getComplexRecords(Signature signature);
    
    public boolean hasDynamicChildren();
    
    public List<? extends IComplexRecord> getDynamicChildren();

    public Signature dynamicSignature();

    public boolean hasSimpleRecord(Signature signature);

    public SimpleRecord<?> getSimpleRecord(Signature signature);

    public boolean hasDynamicValues();
    
    public List<SimpleValueRecord<?>> getDynamicValues();
    
    public boolean containsDynamicValue(Signature signature);
}
