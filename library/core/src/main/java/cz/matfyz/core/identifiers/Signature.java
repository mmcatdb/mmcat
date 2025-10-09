package cz.matfyz.core.identifiers;

import cz.matfyz.core.exception.SignatureException;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.core.utils.UniqueSequentialGenerator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents a signature of a morphism. It can be empty, base or composite.
 */
@JsonSerialize(using = Signature.Serializer.class)
@JsonDeserialize(using = Signature.Deserializer.class)
public class Signature implements Serializable, Comparable<Signature> {

    protected final int[] ids;

    protected Signature(int[] ids) {
        this.ids = ids;
    }

    public static BaseSignature createBase(int id) {
        return new BaseSignature(id);
    }

    protected static Signature createComposite(int[] ids) {
        if (ids.length == 1)
            return new BaseSignature(ids[0]);

        return ids.length == 0 ? empty() : new Signature(ids);
    }

    private static final Signature emptyInstance = new Signature(new int[] {});

    public static Signature empty() {
        return emptyInstance;
    }

    public List<BaseSignature> toBases() {
        var output = new ArrayList<BaseSignature>();
        for (int i = 0; i < ids.length; i++)
            output.add(createBase(ids[i]));

        return output;
    }

    public Signature cutLast() {
        if (ids.length == 0)
            return Signature.empty();

        return createComposite(Arrays.copyOfRange(ids, 0, ids.length - 1));
    }

    public BaseSignature getLast() {
        if (ids.length == 0)
            throw SignatureException.isEmpty();

        return createBase(this.ids[this.ids.length - 1]);
    }

    public Signature cutFirst() {
        if (ids.length == 0)
            return Signature.empty();

        return createComposite(Arrays.copyOfRange(ids, 1, ids.length));
    }

    public BaseSignature getFirst() {
        if (ids.length == 0)
            throw SignatureException.isEmpty();

        return createBase(this.ids[0]);
    }

    public Signature concatenate(Signature other) {
        return createComposite(ArrayUtils.concatenate(ids, other.ids));
    }

    public static Signature concatenate(Signature... signatures) {
        return concatenate(List.of(signatures));
    }

    public static Signature concatenate(Collection<? extends Signature> signatures) {
        final var signatureIds = signatures.stream().map(signature -> signature.ids).toList();
        return createComposite(ArrayUtils.concatenate(signatureIds));
    }

    @SafeVarargs
    public static Signature concatenate(Stream<? extends Signature>... streams) {
        final var ids = new ArrayList<int[]>();
        for (final var stream : streams)
            stream.forEach(signatures -> ids.add(signatures.ids));

        return createComposite(ArrayUtils.concatenate(ids));
    }

    public boolean hasPrefix(Signature other) {
        if (ids.length < other.ids.length)
            return false;

        for (int i = 0; i < other.ids.length; i++)
            if (other.ids[i] != ids[i])
                return false;

        return true;
    }

    public @Nullable Signature cutPrefix(Signature other) {
        if (!hasPrefix(other))
            return null;

        return createComposite(Arrays.copyOfRange(ids, other.ids.length, ids.length));
    }

    public boolean hasSuffix(Signature other) {
        final int offset = ids.length - other.ids.length;
        if (offset < 0)
            return false;

        for (int i = 0; i < other.ids.length; i++)
            if (other.ids[i] != ids[i + offset])
                return false;

        return true;
    }

    public @Nullable Signature cutSuffix(Signature other) {
        if (!hasSuffix(other))
            return null;

        return createComposite(Arrays.copyOfRange(ids, 0, ids.length - other.ids.length));
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

    public Signature longestCommonPrefix(Signature other) {
        final int maxLength = Math.min(ids.length, other.ids.length);
        for (int i = 0; i < maxLength; i++) {
            if (ids[i] != other.ids[i])
                return createComposite(Arrays.copyOfRange(ids, 0, i));
        }

        return createComposite(Arrays.copyOfRange(ids, 0, maxLength));
    }

    public Signature dual() {
        final int n = ids.length;
        if (n == 0)
            return this;

        final int[] array = new int[n];
        for (int i = 0; i < n; i++)
            array[i] = -ids[n - i - 1];

        return createComposite(array);
    }

    public boolean isEmpty() {
        return ids.length == 0;
    }

    public boolean isComposite() {
        return ids.length > 1;
    }

    private static final String SEPARATOR = ".";

    @Override public String toString() {
        return toString(SEPARATOR);
    }

    public String toString(String separator) {
        if (isEmpty())
            return "EMPTY";

        final var sb = new StringBuilder();

        sb.append(ids[0]);
        for (int i = 1; i < ids.length; i++)
            sb.append(separator).append(ids[i]);

        return sb.toString();
    }

    public static Signature fromString(String string) {
        if (string.equals("EMPTY"))
            return empty();

        try {
            final var ids = List.of(string.split("\\" + SEPARATOR)).stream().mapToInt(Integer::parseInt).toArray();
            return createComposite(ids);
        }
        catch (NumberFormatException e) {
            throw SignatureException.invalid(string);
        }
    }

    @Override public boolean equals(Object object) {
        return object instanceof Signature signature && compareTo(signature) == 0;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     */
    @Override public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Arrays.hashCode(this.ids);
        return hash;
    }

    @Override public int compareTo(Signature signature) {
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

    public boolean hasDual() {
        for (final int id : ids)
            if (id < 0)
                return true;

        return false;
    }

    /**
     * Like {@link #concatenate(Signature)} but also removes the duplicities between both signatures.
     * E.g., if this is <code>1.2.3</code> and the <code>path</code> is <code>-3.-2.4</code>, the result is <code>1.4</code>.
     */
    public Signature traverse(Signature other) {
        final int maxCommonLength = Math.min(ids.length, other.ids.length);
        int commonLength = maxCommonLength;
        for (int i = 0; i < maxCommonLength; i++) {
            if (ids[ids.length - i - 1] != -other.ids[i]) {
                commonLength = i;
                break;
            }
        }

        // Now we have the length of the common path. The rest is just a copying.
        final int outputSize = ids.length + other.ids.length - 2 * commonLength;
        final int[] output = new int[outputSize];

        // Copy the rest of this signature.
        final int thisRestLength = ids.length - commonLength;
        for (int i = 0; i < thisRestLength; i++)
            output[i] = ids[i];

        // Copy the rest of the other signature.
        for (int i = 0; i < other.ids.length - commonLength; i++)
            output[thisRestLength + i] = other.ids[commonLength + i];

        return createComposite(output);
    }

    /**
     * A convenience method like {@link #traverse(Signature)} but traverses "backwards".
     * It's equal to <code>this.dual().traverse(other)</code>.
     * Corresponds to a situation where there is a morphism A -> B (this signature) and A -> C (other signature).
     * This method returns the signature of the morphism B -> C.
     * @param other
     * @return
     */
    public Signature traverseBack(Signature other) {
        return dual().traverse(other);
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<Signature> {
        public Serializer() { this(null); }
        public Serializer(Class<Signature> t) { super(t); }

        @Override public void serialize(Signature signature, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(signature.toString());
        }
    }

    public static class Deserializer extends StdDeserializer<Signature> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public Signature deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);
            return Signature.fromString(node.asText());
        }
    }

    // #endregion

    public static class SignatureGenerator {

        private final UniqueSequentialGenerator idGenerator;

        private SignatureGenerator(UniqueSequentialGenerator idGenerator) {
            this.idGenerator = idGenerator;
        }

        public static SignatureGenerator create() {
            return new SignatureGenerator(UniqueSequentialGenerator.create());
        }

        public static SignatureGenerator create(Collection<Signature> current) {
            final var currentIds = current.stream().flatMapToInt(signature -> Arrays.stream(signature.ids))
                .map(Math::abs).toArray();
            return new SignatureGenerator(UniqueSequentialGenerator.create(currentIds));
        }

        public BaseSignature next() {
            return Signature.createBase(idGenerator.next());
        }

    }

}
