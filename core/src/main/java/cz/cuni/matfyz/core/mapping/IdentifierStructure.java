package cz.cuni.matfyz.core.mapping;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author pavel.koupil
 */
public class IdentifierStructure {

    private final Collection<String> properties;

    public IdentifierStructure(Collection<String> properties) {
        this.properties = new ArrayList<>(properties);
    }

    public Collection<String> properties() {
        return properties;
    }

    @Override
    public String toString() {
        return String.join(", ", properties);
    }
    
}
