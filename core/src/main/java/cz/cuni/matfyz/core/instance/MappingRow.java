package cz.cuni.matfyz.core.instance;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * This class represents a relation between two members of two domains ({@link DomainRow}).
 * It corresponds to a single {@link InstanceMorphism}.
 * @author jachym.bartik
 */
@JsonSerialize(using = MappingRow.Serializer.class)
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

    public MappingRow toDual() {
        return new MappingRow(codomainRow, domainRow);
    }

    @Override
    public int compareTo(MappingRow row) {
        // This is not sufficient generally because there might be multiple different mappings between the same two rows.
        // However, it is sufficient in the context of one instance morphisms, i.e., if we compare only mappings that belong to the same morphism.
        int domainCompareResult = domainRow.compareTo(row.domainRow);
        return domainCompareResult != 0 ? domainCompareResult : codomainRow.compareTo(row.codomainRow);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(domainRow).append(" -> ").append(codomainRow);
        
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof MappingRow row && domainRow.equals(row.domainRow) && codomainRow.equals(row.codomainRow);
    }

    public static class Serializer extends StdSerializer<MappingRow> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<MappingRow> t) {
            super(t);
        }

        @Override
        public void serialize(MappingRow row, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("domRow", row.domainRow);
            generator.writePOJOField("codRow", row.codomainRow);
            generator.writeEndObject();
        }

    }
    
}
