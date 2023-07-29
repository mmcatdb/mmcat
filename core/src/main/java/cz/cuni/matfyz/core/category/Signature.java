package cz.cuni.matfyz.core.category;

import cz.cuni.matfyz.core.exception.SignatureException;
import cz.cuni.matfyz.core.utils.ArrayUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * This class represents a signature of a morphism. It can be empty, base or composite.
 * @author jachym.bartik
 */
@JsonSerialize(using = Signature.Serializer.class)
@JsonDeserialize(using = Signature.Deserializer.class)
public class Signature implements Serializable, Comparable<Signature> {

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

    public List<Signature> toBases() {
        var output = new ArrayList<Signature>();
        for (int i = 0; i < ids.length; i++)
            output.add(createBase(ids[i]));

        return output;
    }

    // Evolution extension
    public Signature cutLast() {
        if (ids.length == 0)
            return Signature.createEmpty();

        var newIds = Arrays.copyOfRange(ids, 0, ids.length - 1);
        return createComposite(newIds);
    }

    public Signature getLast() {
        if (ids.length == 0)
            return Signature.createEmpty();

        return createBase(this.ids[this.ids.length - 1]);
    }

    public Signature cutFirst() {
        if (ids.length == 0)
            return Signature.createEmpty();

        var newIds = Arrays.copyOfRange(ids, 1, ids.length);
        return createComposite(newIds);
    }

    public Signature getFirst() {
        if (ids.length == 0)
            return Signature.createEmpty();

        return createBase(this.ids[0]);
    }

    public Signature concatenate(Signature other) {
        return createComposite(ArrayUtils.concatenate(ids, other.ids));
    }

    public static Signature concatenate(Signature... signatures) {
        return concatenate(List.of(signatures));
    }

    public static Signature concatenate(Collection<Signature> signatures) {
        final var signatureIds = signatures.stream().map(signature -> signature.ids).toList();
        return createComposite(ArrayUtils.concatenate(signatureIds));
    }

    public boolean contains(Signature other) {
        if (other.ids.length == 0)
            return true;

        for (int i = 0; i < this.ids.length; i++) {
            if (ids[i] != other.ids[0])
                continue;

            int j = 1;
            while (i + j < ids.length && j < other.ids.length) {
                if (ids[i + j] != other.ids[j])
                    break;
                j++;
            }

            if (j == other.ids.length)
                return true;
        }

        return false;
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

    public boolean isBaseDual() {
        return isBase() && ids[0] < 0;
    }

    public int getBaseValue() {
        return this.isEmpty() ? 0 : this.ids[0];
    }

    private static final String SEPARATOR = ".";

    @Override
    public String toString() {
        if (isEmpty())
            return "EMPTY";

        StringBuilder builder = new StringBuilder();
        
        builder.append(ids[0]);
        for (int i = 1; i < ids.length; i++)
            builder.append(SEPARATOR).append(ids[i]);
            
        return builder.toString();
    }

    public static Signature fromString(String string) {
        if (string.equals("EMPTY"))
            return createEmpty();

        try {
            final var ids = List.of(string.split("\\" + SEPARATOR)).stream().mapToInt(Integer::parseInt).toArray();
            return new Signature(ids);
        }
        catch (NumberFormatException e) {
            throw SignatureException.invalid(string);
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
    
    public boolean hasDualOfAsPrefix(Signature signature) {
        if (signature == null)
            return false;
        
        final Signature dual = signature.dual();
        final int dualLength = dual.ids.length;
        
        if (ids.length < dualLength)
            return false;
        
        for (int i = 0; i < dualLength; i++)
            if (dual.ids[i] != ids[i])
                return false;
        
        return true;
    }
    
    public Signature traverseThrough(Signature path) {
        if (!hasDualOfAsPrefix(path))
            return null;
        
        return createComposite(Arrays.copyOfRange(ids, path.ids.length, ids.length));
    }

    public Signature traverseAlong(Signature path) {
        final var output = new LinkedList<Integer>();
        for (final var id : ids)
            output.add(id);

        for (final var pathId : path.ids) {
            if (output.isEmpty() || pathId != output.getFirst())
                output.addFirst(-pathId);
            else
                output.removeFirst();
        }

        return createComposite(output.stream().mapToInt(Integer::intValue).toArray());
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
            generator.writeString(signature.toString());
        }

    }

    public static class Deserializer extends StdDeserializer<Signature> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Signature deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            return Signature.fromString(node.asText());
        }

    }

    public static class Generator {

        private int max;

        public Generator(Collection<Signature> current) {
            max = current.stream().flatMapToInt(signature -> Arrays.stream(signature.ids))
                .map(Math::abs)
                .reduce(0, Math::max);
        }

        public Signature next() {
            max++;
            return Signature.createBase(max);
        }

    }

}
