package cz.matfyz.core.instance;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

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
 * This class represents a relation between two members of two domains ({@link DomainRow}).
 * It corresponds to a single {@link InstanceMorphism}.
 * @author jachym.bartik
 */
@JsonSerialize(using = MappingRow.Serializer.class)
@JsonDeserialize(using = MappingRow.Deserializer.class)
public class MappingRow implements Serializable, Comparable<MappingRow> {

    private final DomainRow domainRow;
    private final DomainRow codomainRow;

    public DomainRow domainRow() {
        return domainRow;
    }

    public DomainRow codomainRow() {
        return codomainRow;
    }

    public MappingRow(DomainRow domainRow, DomainRow codomainRow) {
        this.domainRow = domainRow;
        this.codomainRow = codomainRow;
    }

    @Override public int compareTo(MappingRow row) {
        // This is not sufficient generally because there might be multiple different mappings between the same two rows.
        // However, it is sufficient in the context of one instance morphisms, i.e., if we compare only mappings that belong to the same morphism.
        int domainCompareResult = domainRow.compareTo(row.domainRow);
        return domainCompareResult != 0 ? domainCompareResult : codomainRow.compareTo(row.codomainRow);
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(domainRow).append(" -> ").append(codomainRow);

        return builder.toString();
    }

    @Override public boolean equals(Object object) {
        return object instanceof MappingRow row && domainRow.equals(row.domainRow) && codomainRow.equals(row.codomainRow);
    }

    public static class Serializer extends StdSerializer<MappingRow> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<MappingRow> t) {
            super(t);
        }

        @Override public void serialize(MappingRow row, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("dom", row.domainRow.serializationId);
            generator.writeNumberField("cod", row.codomainRow.serializationId);
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<MappingRow> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override public MappingRow deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final var domRows = (Map<Integer, DomainRow>) context.getAttribute("domRows");
            final var codRows = (Map<Integer, DomainRow>) context.getAttribute("codRows");

            final int domId = node.get("dom").asInt();
            final DomainRow dom = domRows.get(domId);
            final int codId = node.get("cod").asInt();
            final DomainRow cod = codRows.get(codId);

            return new MappingRow(dom, cod);
        }

    }

}
