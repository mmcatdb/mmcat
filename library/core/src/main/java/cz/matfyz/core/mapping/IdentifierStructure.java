package cz.matfyz.core.mapping;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class IdentifierStructure {

    private final SortedSet<String> properties;

    public IdentifierStructure(Collection<String> properties) {
        this.properties = new TreeSet<>(properties);
    }

    public Collection<String> properties() {
        return properties;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override public String toString() {
        return String.join(", ", properties);
    }

}
