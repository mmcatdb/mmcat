package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Id is a set of signatures (each corresponding to a base or a composite morphism).
 * @author jachymb.bartik
 */
public class Id implements Comparable<Id>, JSONConvertible
{
    private final SortedSet<Signature> signatures;
    
    public SortedSet<Signature> signatures()
    {
        return signatures;
    }

	public Id(Set<Signature> signatures)
    {
        assert signatures.size() > 0 : "Empty signature array passed to Id constructor.";
		this.signatures = new TreeSet<>(signatures);
	}
    
    public Id(Collection<Signature> signatures)
    {
        assert signatures.size() > 0 : "Empty signature array passed to Id constructor.";
		this.signatures = new TreeSet<>(signatures);
	}

    // There must be at least one signature
	public Id(Signature... signatures)
    {
        assert signatures.length > 0 : "Empty signature array passed to Id constructor.";
		this.signatures = new TreeSet<>(List.of(signatures));
	}
    
    public static Id Empty()
    {
        return new Id(Signature.Empty());
    }
            

	@Override
	public int compareTo(Id id)
    {
        int sizeResult = signatures.size() - id.signatures.size();
        if (sizeResult != 0)
            return sizeResult;
        
        Iterator<Signature> iterator = id.signatures.iterator();
        
        for (Signature signature : signatures)
        {
            int signatureResult = signature.compareTo(iterator.next());
            if (signatureResult != 0)
                return signatureResult;
        }
        
        return 0;
	}
    
    @Override
	public String toString()
    {
		StringBuilder builder = new StringBuilder();

		builder.append("(");
        for (Signature signature : signatures.headSet(signatures.last()))
            builder.append(signature).append(", ");
        builder.append(signatures.last()).append(")");

        return builder.toString();
	}
    
    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Id> {

        @Override
        protected JSONObject _toJSON(Id object) throws JSONException {
            var output = new JSONObject();
    
            var signatures = new JSONArray(object.signatures.stream().map(signature -> signature.toJSON()).toList());
            output.put("signatures", signatures);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<Id> {
    
        @Override
        protected Id _fromJSON(JSONObject jsonObject) throws JSONException {
            var signaturesArray = jsonObject.getJSONArray("signatures");
            var signatures = new TreeSet<Signature>();
            var builder = new Signature.Builder();
            for (int i = 0; i < signaturesArray.length(); i++)
                signatures.add(builder.fromJSON(signaturesArray.getJSONObject(i)));

            return new Id(signatures);
        }
    
    }
}
