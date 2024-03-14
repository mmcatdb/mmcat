package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Common ancestor for the access path tree. It can be a {@link ComplexProperty} or a {@link SimpleProperty}.
 * Each node is a tuple (name, context, value).
 * @author pavel.koupil, jachym.bartik
 */
@JsonDeserialize(using = AccessPath.Deserializer.class)
public abstract class AccessPath implements Printable {

    protected final Signature signature;

    public Signature signature() {
        return signature;
    }

    protected final Name name;

    public Name name() {
        return name;
    }

    // TODO v3
    // This should be determined by two things:
    //  - if the min of the morphism is ONE, this should be true
    //  - if not (or), the user should decide
    //  - like it could be determined solely by the min of the morphism (if the morphism is not array), but what to do when it is array?
    protected final boolean isRequired = false;

    public boolean isRequired() {
        return isRequired;
    }

    protected AccessPath(Name name, Signature signature) {
        this.name = name;
        this.signature = signature;
    }

    protected abstract boolean hasSignature(Signature signature);

    /**
     * Find the path to the given signature in and return the properties along the way.
     * The list is "reversed", meaning that the last subpath is first and the root property is last.
     * If the signature isn't found, null is returned.
     */
    protected abstract @Nullable List<AccessPath> getPropertyPathInternal(Signature signature);

    public abstract AccessPath tryGetSubpathForObject(Key key, SchemaCategory schema);

    @Override public boolean equals(Object object) {
        return object instanceof AccessPath path && name.equals(path.name);
    }

    public static class Deserializer extends StdDeserializer<AccessPath> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader simplePropertyJsonReader = new ObjectMapper().readerFor(SimpleProperty.class);
        private static final ObjectReader complexPropertyJsonReader = new ObjectMapper().readerFor(ComplexProperty.class);

        @Override public AccessPath deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            return node.has("subpaths")
                ? complexPropertyJsonReader.readValue(node)
                : simplePropertyJsonReader.readValue(node);
        }

    }

}
