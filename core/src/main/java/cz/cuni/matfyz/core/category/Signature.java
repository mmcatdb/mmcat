package cz.cuni.matfyz.core.category;

import cz.cuni.matfyz.core.serialization.FromJSONArrayBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONArrayConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONArrayConverterBase;
import cz.cuni.matfyz.core.utils.ArrayUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class represents a signature of a morphism. It can be empty, base or composite.
 * @author jachym.bartik
 */
@JsonSerialize(using = Signature.Serializer.class)
public class Signature implements Serializable, Comparable<Signature>, JSONArrayConvertible {

    private final int[] ids;
    
    private Signature(int[] ids) {
        this.ids = ids;
    }
    
    public static Signature createBase(int id) {
        return new Signature(new int[] { id });
    }

    private static Signature createComposite(int[] ids) {
        return ids.length == 0 ? createEmpty() : new Signature(ids);
    }

    private static final Signature emptyObject = new Signature(new int[] {});

    public static Signature createEmpty() {
        return emptyObject;
    }

    public int[] ids() {
        return this.ids.clone();
    }

    public List<Signature> toBases() {
        var output = new ArrayList<Signature>();
        for (int id : ids)
            output.add(createBase(id));

        return output;
    }

    public List<Signature> toBasesReverse() {
        var output = new ArrayList<Signature>();
        for (int i = ids.length - 1; i >= 0; i--)
            output.add(createBase(ids[i]));

        return output;
    }

    // Evolution extension
    public Signature cutLast() {
        if (ids.length == 0)
            return Signature.createEmpty();

        var newIds = Arrays.copyOfRange(ids, 1, ids.length);
        return createComposite(newIds);
    }

    public Signature getLast() {
        if (ids.length == 0)
            return Signature.createEmpty();

        return createBase(this.ids[0]);
    }

    public Signature cutFirst() {
        if (ids.length == 0)
            return Signature.createEmpty();

        var newIds = Arrays.copyOfRange(ids, 0, ids.length - 1);
        return createComposite(newIds);
    }

    public Signature getFirst() {
        if (ids.length == 0)
            return Signature.createEmpty();

        return createBase(this.ids[this.ids.length - 1]);
    }

    public Signature concatenate(Signature other) {
        return createComposite(ArrayUtils.concatenate(other.ids, ids));
    }

    public static Signature concatenate(Signature... signatures) {
        return concatenate(List.of(signatures));
    }

    public static Signature concatenate(Collection<Signature> signatures) {
        var signaturesIds = new ArrayList<>(signatures.stream().map(signature -> signature.ids).toList());
        Collections.reverse(signaturesIds);
        return createComposite(ArrayUtils.concatenate(signaturesIds));
    }
    
    public Signature dual() {
        int n = ids.length;
        if (n == 0)
            return this;
                    
        int[] array = new int[n];
        for (int i = 0; i < n; i++)
            array[i] = - ids[n - i - 1];
        
        return createComposite(array);
    }
    
    public enum Type {
        EMPTY,      // The corresponding morphism is an identity.
        BASE,       // The length of the signature is exactly one (i.e. it's a signature of morphism between two neigbours in the schema category graph).
        COMPOSITE,  // The signature consists of multiple (i.e. >= 2) base signatures.
        //NULL        // There is no morphism corresponding to given signature. This means the access path's property accessible via this signature is an auxiliary property grouping one or more properties together.
    }
    
    public Type getType() {
        if (isEmpty())
            return Type.EMPTY;
        if (isBase())
            return Type.BASE;
        return Type.COMPOSITE;
    }

    public boolean isEmpty() {
        return ids.length == 0;
    }

    public boolean isBase() {
        return ids.length == 1;
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "EMPTY";

        StringBuilder builder = new StringBuilder();
        
        builder.append(ids[0]);
        for (int i = 1; i < ids.length; i++)
            builder.append(".").append(ids[i]);
            
        return builder.toString();
    }

    public static Signature fromString(String string) {
        if (string.equals("EMPTY"))
            return createEmpty();

        try {
            final var ids = List.of(string.split("\\.")).stream().mapToInt(Integer::parseInt).toArray();
            return new Signature(ids);
        }
        catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Signature signature && compareTo(signature) == 0;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Arrays.hashCode(this.ids);
        return hash;
    }
    
    @Override
    public int compareTo(Signature signature) {
        if (this == signature)
            return 0;

        final int lengthDifference = ids.length - signature.ids.length;
        
        return lengthDifference != 0 ? lengthDifference : compareIdsWithSameLength(signature.ids);
    }
    
    private int compareIdsWithSameLength(int[] anotherIds) {
        for (int i = 0; i < ids.length; i++) {
            final int idDifference = ids[i] - anotherIds[i];
            if (idDifference != 0)
                return idDifference;
        }
        return 0;
    }
    
    public boolean hasDualOfAsSuffix(Signature signature) {
        if (signature == null)
            return false;
        
        Signature dual = signature.dual();
        int dualLength = dual.ids.length;
        
        if (ids.length < dualLength)
            return false;
        
        for (int i = 0; i < dualLength; i++)
            if (dual.ids[i] != ids[i + ids.length - dualLength])
                return false;
        
        return true;
    }
    
    public Signature traverseThrough(Signature signature) {
        if (!hasDualOfAsSuffix(signature))
            return null;
        
        int length = ids.length - signature.ids.length;
        return createComposite(Arrays.copyOfRange(ids, 0, length));
    }

    public Signature traverseAlong(Signature path) {
        var output = new LinkedList<Integer>();
        for (var id : ids)
            output.add(id);

        for (int i = path.ids.length - 1; i >= 0; i--) {
            var pathId = path.ids[i];
            var lastId = output.getLast();
            if (lastId == null)
                output.addFirst(-pathId);
            else if (lastId == pathId)
                output.removeLast();
            else
                output.addLast(-pathId);
        }

        return createComposite(output.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public JSONArray toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONArrayConverterBase<Signature> {

        @Override
        protected JSONArray innerToJSON(Signature object) throws JSONException {
            return new JSONArray(object.ids);
        }
    
    }
    
    public static class Builder extends FromJSONArrayBuilderBase<Signature> {
    
        @Override
        protected Signature innerFromJSON(JSONArray jsonArray) throws JSONException {
            var ids = new int[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
                ids[i] = jsonArray.getInt(i);
            
            return Signature.createComposite(ids);
        }
    
    }

    public static class Serializer extends StdSerializer<Signature> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<Signature> t) {
            super(t);
        }

        @Override
        public void serialize(Signature signature, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeArray(signature.ids, 0, signature.ids.length);
        }

    }

}
