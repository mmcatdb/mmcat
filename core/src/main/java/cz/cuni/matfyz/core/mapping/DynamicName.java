package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.record.DynamicRecordName;
import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author jachym.bartik
 */
public class DynamicName extends Name
{
    private final Signature signature;

    public Signature signature()
    {
        return signature;
    }
    
    public DynamicName(Signature signature)
    {
        this.signature = signature;
    }
    
    public DynamicRecordName toRecordName(String dynamicNameValue)
    {
        return new DynamicRecordName(dynamicNameValue, signature);
    }
    
    @Override
	public String toString()
    {
        return signature.toString();
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof DynamicName dynamicName && signature.equals(dynamicName.signature);
    }
}
