package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.schema.SchemaMorphism;

import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceMorphism implements Identified<InstanceMorphism, Signature> {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceMorphism.class);

    public final SchemaMorphism schema;

    private final SortedSet<MappingRow> mappings = new TreeSet<>();

    InstanceMorphism(SchemaMorphism schemaMorphism) {
        this.schema = schemaMorphism;
    }

    public boolean isEmpty() {
        return mappings.isEmpty();
    }

    public boolean isBase() {
        return this.schema.isBase();
    }

    public MappingRow createMapping(DomainRow domainRow, DomainRow codomainRow) {
        final var mapping = new MappingRow(domainRow, codomainRow);
        addMapping(mapping);
        return mapping;
    }

    public static MappingRow createMappingForEdge(InstanceCategory instance, SchemaEdge edge, DomainRow fromRow, DomainRow toRow) {
        final InstanceMorphism morphism = instance.getMorphism(edge.morphism());

        if (!edge.direction()) {
            var swap = fromRow;
            fromRow = toRow;
            toRow = swap;
        }

        return morphism.createMapping(fromRow, toRow);
    }

    private void addMapping(MappingRow mapping) {
        mappings.add(mapping);
        mapping.domainRow().setMappingFrom(this, mapping);
        mapping.codomainRow().addMappingTo(this, mapping);
    }

    void removeMapping(MappingRow mapping) {
        mappings.remove(mapping);
        mapping.domainRow().unsetMappingFrom(this);
        mapping.codomainRow().removeMappingTo(this, mapping);
    }

    SortedSet<MappingRow> allMappings() {
        return mappings;
    }

    // Identification

    @Override public Signature identifier() {
        return schema.signature();
    }

    @Override public boolean equals(Object other) {
        return other instanceof InstanceMorphism instanceMorphism && instanceMorphism.schema.equals(schema);
    }

    @Override public int hashCode() {
        return schema.hashCode();
    }

    // Debug

    @Override public String toString() {
        var builder = new StringBuilder();

        builder.append("\tSignature: ").append(schema.signature())
            .append("\tDom: ").append(schema.dom().key())
            .append("\tCod: ").append(schema.cod().key())
            .append("\n");

        builder.append("\tValues:\n");
        //for (Set<ActiveMappingRow> set : mappings.values())
        //    for (ActiveMappingRow row : set)
        for (MappingRow row : allMappings())
            builder.append("\t\t").append(row).append("\n");

        return builder.toString();
    }

}
