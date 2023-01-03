package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Id is a set of signatures (each corresponding to a base or a composite morphism).
 * @author jachymb.bartik
 */
@JsonSerialize(using = SignatureId.Serializer.class)
public class SignatureId implements Serializable, Comparable<SignatureId>, JSONConvertible {
    
    private final SortedSet<Signature> signatures;
    
    // TODO make immutable
    public SortedSet<Signature> signatures() {
        return signatures;
    }

    public SignatureId(Set<Signature> signatures) {
        this(new TreeSet<>(signatures));
    }
    
    // There must be at least one signature
    public SignatureId(Signature... signatures) {
        this(new TreeSet<>(List.of(signatures)));
    }
    
    public static SignatureId createEmpty() {
        return new SignatureId(Signature.createEmpty());
    }
    
    private SignatureId(SortedSet<Signature> signatures) {
        this.signatures = signatures;
    }
    
    public boolean hasSignature(Signature signature) {
        return this.signatures.contains(signature);
    }

    public boolean hasOnlyEmptySignature() {
        return this.signatures.size() == 1 && this.signatures.first().isEmpty();
    }

    @Override
    public int compareTo(SignatureId id) {
        int sizeResult = signatures.size() - id.signatures.size();
        if (sizeResult != 0)
            return sizeResult;
        
        Iterator<Signature> iterator = id.signatures.iterator();
        
        for (Signature signature : signatures) {
            int signatureResult = signature.compareTo(iterator.next());
            if (signatureResult != 0)
                return signatureResult;
        }
        
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof SignatureId id && compareTo(id) == 0;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        for (Signature signature : signatures.headSet(signatures.last()))
            builder.append(signature).append(", ");
        builder.append(signatures.last());
        builder.append(")");

        return builder.toString();
    }
    
    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SignatureId> {

        @Override
        protected JSONObject innerToJSON(SignatureId object) throws JSONException {
            var output = new JSONObject();
    
            var signatures = new JSONArray(object.signatures.stream().map(Signature::toJSON).toList());
            output.put("signatures", signatures);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<SignatureId> {
    
        @Override
        protected SignatureId innerFromJSON(JSONObject jsonObject) throws JSONException {
            var signaturesArray = jsonObject.getJSONArray("signatures");
            var signatures = new TreeSet<Signature>();
            var builder = new Signature.Builder();
            for (int i = 0; i < signaturesArray.length(); i++)
                signatures.add(builder.fromJSON(signaturesArray.getJSONObject(i)));

            return new SignatureId(signatures);
        }
    
    }

    public static class Serializer extends StdSerializer<SignatureId> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SignatureId> t) {
            super(t);
        }

        @Override
        public void serialize(SignatureId signatureId, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (final var signature : signatureId.signatures) {
                generator.writeObject(signature);
            }
            generator.writeEndArray();
        }

    }

}
