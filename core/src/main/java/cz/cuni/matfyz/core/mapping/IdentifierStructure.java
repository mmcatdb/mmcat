package cz.cuni.matfyz.core.mapping;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author pavel.koupil
 */
public class IdentifierStructure {

    private final SortedSet<String> properties;

    public IdentifierStructure(Collection<String> properties) {
        this.properties = new TreeSet<>(properties);
    }

    public Collection<String> properties() {
        return properties;
    }

    @Override
    public String toString() {
        return String.join(", ", properties);
    }
    
}
