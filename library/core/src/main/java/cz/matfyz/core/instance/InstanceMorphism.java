package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceMorphism implements Identified<InstanceMorphism, Signature> {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceMorphism.class);

    private final SchemaMorphism schemaMorphism;
    private final List<InstanceMorphism> bases;
    private final InstanceObject dom;
    private final InstanceObject cod;

    private final SortedSet<MappingRow> mappings = new TreeSet<>();

    public InstanceMorphism(SchemaMorphism schemaMorphism, InstanceObject dom, InstanceObject cod, InstanceCategory category) {
        this.schemaMorphism = schemaMorphism;
        this.dom = dom;
        this.cod = cod;
        this.bases = isBase()
            ? List.of(this)
            : List.of(signature().toBases().stream().map(category::getMorphism).toArray(InstanceMorphism[]::new));
    }

    public boolean isEmpty() {
        return mappings.isEmpty();
    }

    public boolean isBase() {
        return this.schemaMorphism.isBase();
    }

    /**
     * Returns base morphisms in the order they need to be traversed (i.e., the first one has the same domainObject as this).
     * @return
     */
    public List<InstanceMorphism> bases() {
        return bases;
    }

    public InstanceMorphism lastBase() {
        return bases.get(bases.size() - 1);
    }

    public void createMapping(DomainRow domainRow, DomainRow codomainRow) {
        var mapping = new MappingRow(domainRow, codomainRow);

        addMapping(mapping);

        // TODO shouldn't there be a merge?
    }

    // TODO potentially dangerous function, left for testing purposes.
    public void addMapping(MappingRow mapping) {
        mappings.add(mapping);
        mapping.domainRow().addMappingFrom(this, mapping);
        mapping.codomainRow().addMappingTo(this, mapping);
    }

    public void removeMapping(MappingRow mapping) {
        mappings.remove(mapping);
        mapping.domainRow().removeMappingFrom(this, mapping);
        mapping.codomainRow().removeMappingTo(this, mapping);
    }

    public SortedSet<MappingRow> allMappings() {
        return mappings;
    }

    public Signature signature() {
        return schemaMorphism.signature();
    }

    public Min min() {
        return schemaMorphism.min();
    }

    public InstanceObject dom() {
        return dom;
    }

    public InstanceObject cod() {
        return cod;
    }

    // Identification

    @Override public Signature identifier() {
        return schemaMorphism.signature();
    }

    @Override public boolean equals(Object other) {
        return other instanceof InstanceMorphism instanceMorphism && instanceMorphism.schemaMorphism.equals(schemaMorphism);
    }

    @Override public int hashCode() {
        return schemaMorphism.hashCode();
    }

    // Identification

    @Override public String toString() {
        var builder = new StringBuilder();

        builder.append("\tSignature: ").append(signature())
            .append("\tDom: ").append(dom.key())
            .append("\tCod: ").append(cod.key())
            .append("\n");

        builder.append("\tValues:\n");
        //for (Set<ActiveMappingRow> set : mappings.values())
        //    for (ActiveMappingRow row : set)
        for (MappingRow row : allMappings())
            builder.append("\t\t").append(row).append("\n");

        return builder.toString();
    }

}
