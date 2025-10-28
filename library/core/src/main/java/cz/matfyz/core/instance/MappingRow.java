package cz.matfyz.core.instance;

/**
 * This class represents a relation between two members of two domains ({@link DomainRow}).
 * It corresponds to a single {@link InstanceMorphism}.
 */
public class MappingRow implements Comparable<MappingRow> {

    private final DomainRow dom;
    private final DomainRow cod;

    public DomainRow dom() {
        return dom;
    }

    public DomainRow cod() {
        return cod;
    }

    public MappingRow(DomainRow dom, DomainRow cod) {
        this.dom = dom;
        this.cod = cod;
    }

    @Override public int compareTo(MappingRow other) {
        if (this == other)
            return 0;

        // This is not sufficient generally because there might be multiple different mappings between the same two rows.
        // However, it is sufficient in the context of one instance morphisms, i.e., if we compare only mappings that belong to the same morphism.
        final int domComparison = dom.compareTo(other.dom);
        return domComparison != 0 ? domComparison : cod.compareTo(other.cod);
    }

    @Override public String toString() {
        return new StringBuilder()
            .append(dom.superId).append(" -> ").append(cod.superId)
            .toString();
    }

    // There is no notion of equality for MappingRow, so we don't override equals.
    // In practice, they are identified by their domain and codomain rows. However, they should be unique by these values in the context of one instance morphism.
    // So, if two rows have the same values, they have to be referentially equal.

}
