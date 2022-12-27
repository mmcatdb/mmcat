package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO
 * @author jachymb.bartik
 */
public class ObjectIds implements Serializable, JSONConvertible {

    public enum Type {
        Signatures, // Its a set of signatures.
        Value, // Its a simple string value.
        Generated // Its a simple string value that has to be automatically generated.
    }
    
    private final SortedSet<SignatureId> signatureIds;
    private final Type type;
    
    // TODO disable this method eventually and fix all other methods that rely on it.
    // The reason is that this whole object was introduced because we want to behave differently to the different types of ids - so there ain't be no function that unifies them back together.
    public SortedSet<SignatureId> toSignatureIds() {
        return isSignatures() ? new TreeSet<>(signatureIds) : new TreeSet<>(Set.of(SignatureId.createEmpty()));
    }

    public ObjectIds(Set<SignatureId> signatureIds) {
        this(new TreeSet<>(signatureIds));
    }
    
    public ObjectIds(Collection<SignatureId> signatureIds) {
        this(new TreeSet<>(signatureIds));
    }

    // There must be at least one signature
    public ObjectIds(SignatureId... signatureIds) {
        this(new TreeSet<>(List.of(signatureIds)));
    }

    public ObjectIds(Signature... signatures) {
        this(new TreeSet<>(List.of(new SignatureId(signatures))));
    }
    
    public static ObjectIds createValue() {
        return new ObjectIds(Type.Value);
    }

    public static ObjectIds createGenerated() {
        return new ObjectIds(Type.Generated);
    }
    
    private ObjectIds(SortedSet<SignatureId> signatures) {
        this.signatureIds = signatures;
        this.type = Type.Signatures;
    }

    private ObjectIds(Type type) {
        assert(type != Type.Signatures);

        this.signatureIds = null;
        this.type = type;
    }

    public boolean isSignatures() {
        return type == Type.Signatures;
    }

    public boolean isValue() {
        return type == Type.Value;
    }

    public boolean isGenerated() {
        return type == Type.Generated;
    }

    public SignatureId generateDefaultSuperId() {
        if (type != Type.Signatures)
            return SignatureId.createEmpty();
        
        final var allSignatures = new TreeSet<Signature>();
        signatureIds.forEach(id -> allSignatures.addAll(id.signatures()));

        return new SignatureId(allSignatures);
    }
    
    @Override
    public String toString() {
        if (type == Type.Value)
            return "_VALUE";

        if (type == Type.Generated)
            return "_GENERATED";

        StringBuilder builder = new StringBuilder();

        builder.append("(");
        for (SignatureId signatureId : signatureIds.headSet(signatureIds.last()))
            builder.append(signatureId).append(", ");
        builder.append(signatureIds.last());
        builder.append(")");

        return builder.toString();
    }
    
    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<ObjectIds> {

        @Override
        protected JSONObject innerToJSON(ObjectIds object) throws JSONException {
            var output = new JSONObject();
    
            var signatures = new JSONArray(object.signatureIds.stream().map(signature -> signature.toJSON()).toList());
            output.put("signatures", signatures);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<ObjectIds> {
    
        @Override
        protected ObjectIds innerFromJSON(JSONObject jsonObject) throws JSONException {
            var type = Type.valueOf(jsonObject.getString("type"));
            if (type == Type.Value || type == Type.Generated)
                return new ObjectIds(type);

            var signatureIdsArray = jsonObject.getJSONArray("signatureIds");
            var signatureIds = new TreeSet<SignatureId>();
            var builder = new SignatureId.Builder();
            for (int i = 0; i < signatureIdsArray.length(); i++)
                signatureIds.add(builder.fromJSON(signatureIdsArray.getJSONObject(i)));

            return new ObjectIds(signatureIds);
        }
    
    }
}
