package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * This is a complex record which only purpose is to store one complex record child.
 * Imagine property A with dynamic (complex) children B. There will be one record A with multiple children B. But, there should be multiple objects that corresponds to "type" A, each with exactly one corresponding object B.
 * Thus when record A is processed (in the fetchSids function), a different instance of this class is returned for each record B.
 * @author jachymb.bartik
 */
public class DynamicRecordWrapper implements IComplexRecord
{
    private final IComplexRecord source;
    private final IComplexRecord content;

	public DynamicRecordWrapper(IComplexRecord source, IComplexRecord content)
    {
        this.source = source;
        this.content = content;
	}

    public RecordName name()
    {
        return source.name();
    }

    public boolean hasComplexRecords(Signature signature)
    {
        return signature.equals(source.dynamicSignature()) || source.hasComplexRecords(signature);
    }

    public List<? extends IComplexRecord> getComplexRecords(Signature signature)
    {
        return signature.equals(source.dynamicSignature()) ? List.of(content) : source.getComplexRecords(signature);
    }

    public boolean hasDynamicChildren()
    {
        return false;
    }

    public List<? extends IComplexRecord> getDynamicChildren()
    {
        return new ArrayList<>();
    }

    public Signature dynamicSignature()
    {
        return source.dynamicSignature();
    }

    public boolean hasSimpleRecord(Signature signature)
    {
        return source.hasSimpleRecord(signature);
    }

    public SimpleRecord<?> getSimpleRecord(Signature signature)
    {
        return source.getSimpleRecord(signature);
    }

    public boolean hasDynamicValues()
    {
        return source.hasDynamicValues();
    }

    public List<SimpleValueRecord<?>> getDynamicValues()
    {
        return source.getDynamicValues();
    }

    public boolean containsDynamicValue(Signature signature)
    {
        return source.containsDynamicValue(signature);
    }
}
