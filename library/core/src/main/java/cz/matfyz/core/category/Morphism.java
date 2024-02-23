package cz.matfyz.core.category;

/**
 * @author pavel.koupil, jachym.bartik
 */
public interface Morphism extends Comparable<Morphism> {

    // Beware that the cardinality of morphism doesn't mean the cardinality from the relational point of view.
    // For example, 1..1 means there is exactly one morphism (which is a monomorphism i.e. an injection).
    // However, it doesn't mean there is one object from the codomain for each object from the domain.
    enum Min {
        ZERO,
        ONE,
    }

    enum Max {
        ONE,
        STAR,
    }

    enum Tag {
        isa,
        role,
        projection,
        key,
    }

    CategoricalObject dom();

    CategoricalObject cod();

    Signature signature();

    Min min();

    @Override default int compareTo(Morphism morphism) {
        return signature().compareTo(morphism.signature());
    }

}
