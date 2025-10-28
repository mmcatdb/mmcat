package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Common ancestor for the access path tree. It can be a {@link ComplexProperty} or a {@link SimpleProperty}.
 */
@JsonDeserialize(using = AccessPath.Deserializer.class)
public abstract class AccessPath implements Printable, Serializable {

    protected final Signature signature;

    /** A path to the property from its parent property (in the {@link SchemaCategory} graph). */
    public Signature signature() {
        return signature;
    }

    protected final Name name;

    /** A path to the property from its parent property (in the hierarchy of the given datasource). */
    public Name name() {
        return name;
    }

    /**
     * If this property represents a (multidimensional) array, the indexes are mapped via these signatures.
     * Can contain nulls for dimensions that are not mapped (so the order is preserved).
     * The array dimension of the property is therefore equal to the size of this list.
     */
    protected final List<@Nullable Signature> indexSignatures;

    protected List<@Nullable Signature> indexSignatures() {
        return indexSignatures;
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

    protected AccessPath(Name name, Signature signature, List<@Nullable Signature> indexSignatures) {
        this.name = name;
        this.signature = signature;
        this.indexSignatures = List.copyOf(indexSignatures);
    }

    /**
     * Find the path to the given signature in and return the properties along the way.
     * The list is "reversed", meaning that the last subpath is first and the root property is last.
     * If the signature isn't found, null is returned.
     */
    protected abstract @Nullable List<AccessPath> getPropertyPathInternal(Signature signature);

    public abstract @Nullable AccessPath tryGetSubpathForObjex(Key key, SchemaCategory schema);

    /**
     * Creates copy of this property but with a new name and signature.
     * @param replacedNames If not null, all dynamic names in children will be replaced. The results will be added to this map.
     */
    protected abstract AccessPath copyForReplacement(Name name, Signature signature, @Nullable Map<DynamicName, DynamicNameReplacement> replacedNames);

    @Override public boolean equals(Object object) {
        return object instanceof AccessPath path && name.equals(path.name);
    }

    // #region Serialization

    public static class Deserializer extends StdDeserializer<AccessPath> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public AccessPath deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);
            return node.has("subpaths")
                ? codec.treeToValue(node, ComplexProperty.class)
                : codec.treeToValue(node, SimpleProperty.class);
        }
    }

    // #endregion

}
