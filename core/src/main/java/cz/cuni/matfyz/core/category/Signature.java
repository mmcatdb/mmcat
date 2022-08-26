package cz.cuni.matfyz.core.category;

import cz.cuni.matfyz.core.mapping.IContext;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.utils.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a signature of a morphism. It can be empty, base, composite or null.
 * @author jachym.bartik
 */
public class Signature implements Serializable, Comparable<Signature>, IContext, JSONConvertible {

    private final int[] ids;
    private final boolean isNull;
    
    private Signature(int[] ids, boolean isNull) {
        this.ids = isNull ? new int[] {} : ids;
        this.isNull = isNull;
    }
    
    public Signature(int id) {
        this.ids = new int[] { id };
        this.isNull = false;
    }

    public int[] ids() {
        return this.ids.clone();
    }

    public List<Signature> toBases() {
        if (this.isNull)
            return List.of(this);

        var output = new ArrayList<Signature>();
        for (int id : ids)
            output.add(new Signature(id));

        return output;
    }

    public List<Signature> toBasesReverse() {
        if (this.isNull)
            return List.of(this);

        var output = new ArrayList<Signature>();
        for (int i = ids.length - 1; i >= 0; i--)
            output.add(new Signature(ids[i]));

        return output;
    }

    // Evolution extension
    public Signature cutLast() {
        if (isNull)
            return Signature.createNull();

        if (ids.length == 0)
            return Signature.createEmpty();

        var newIds = Arrays.copyOfRange(ids, 1, ids.length);
        return new Signature(newIds, false);
    }

    public Signature getLast() {
        if (isNull)
            return Signature.createNull();

        if (ids.length == 0)
            return Signature.createEmpty();

        return new Signature(this.ids[0]);
    }

    public Signature cutFirst() {
        if (isNull)
            return Signature.createNull();

        if (ids.length == 0)
            return Signature.createEmpty();

        var newIds = Arrays.copyOfRange(ids, 0, ids.length - 1);
        return new Signature(newIds, false);
    }

    public Signature getFirst() {
        if (isNull)
            return Signature.createNull();

        if (ids.length == 0)
            return Signature.createEmpty();

        return new Signature(this.ids[this.ids.length - 1]);
    }

    public Signature concatenate(Signature other) {
        return new Signature(ArrayUtils.concatenate(other.ids, ids), false);
    }

    public static Signature concatenate(Collection<Signature> signatures) {
        var signaturesIds = new ArrayList<>(signatures.stream().map(signature -> signature.ids).toList());
        Collections.reverse(signaturesIds);
        return new Signature(ArrayUtils.concatenate(signaturesIds), false);
    }

    public static Signature createEmpty() {
        return new Signature(new int[] {}, false);
    }
    
    public static Signature createNull() {
        return new Signature(new int[] {}, true);
    }
    
    public Signature dual() {
        int n = ids.length;
        if (n == 0)
            return this;
                    
        int[] array = new int[n];
        for (int i = 0; i < n; i++)
            array[i] = - ids[n - i - 1];
        
        return new Signature(array, false);
    }
    
    public enum Type {
        EMPTY,      // The corresponding morphism is an identity.
        BASE,       // The length of the signature is exactly one (i.e. it's a signature of morphism between two neigbours in the schema category graph).
        COMPOSITE,  // The signature consists of multiple (i.e. >= 2) base signatures.
        NULL        // There is no morphism corresponding to given signature. This means the access path's property accessible via this signature is an auxiliary property grouping one or more properties together.
    }
    
    public Type getType() {
        if (isNull)
            return Type.NULL;
        if (isEmpty())
            return Type.EMPTY;
        if (ids.length == 1)
            return Type.BASE;
        return Type.COMPOSITE;
    }

    public boolean isEmpty() {
        return ids.length == 0;
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "_EMPTY";
        
        StringBuilder builder = new StringBuilder();
        
        builder.append(ids[0]);
        for (int i = 1; i < ids.length; i++)
            builder.append(".").append(ids[i]);
            
        return builder.toString();
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
        return length == 0 ? Signature.createEmpty() : new Signature(Arrays.copyOfRange(ids, 0, length), false);
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

        return new Signature(output.stream().mapToInt(Integer::intValue).toArray(), false);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Signature> {

        @Override
        protected JSONObject innerToJSON(Signature object) throws JSONException {
            var output = new JSONObject();
    
            var ids = new JSONArray(object.ids);
            output.put("ids", ids);
            output.put("isNull", object.isNull);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<Signature> {
    
        @Override
        protected Signature innerFromJSON(JSONObject jsonObject) throws JSONException {
            var idsArray = jsonObject.getJSONArray("ids");
            var ids = new int[idsArray.length()];
            for (int i = 0; i < idsArray.length(); i++)
                ids[i] = idsArray.getInt(i);
            
            var isNull = jsonObject.getBoolean("isNull");

            return new Signature(ids, isNull);
        }
    
    }

}
