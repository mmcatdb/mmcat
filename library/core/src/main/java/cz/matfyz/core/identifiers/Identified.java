package cz.matfyz.core.identifiers;

public interface Identified<T extends Identified<T, K>, K extends Comparable<K>> extends Comparable<T> {

    K identifier();

    default int compareTo(T other) {
        return identifier().compareTo(other.identifier());
    }

}
