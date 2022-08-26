package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author jachym.bartik
 */
public class Reference {

    private final Name name;
    private final Set<Signature> properties;

    public Reference(Name name, Collection<Signature> properties) {
        this.name = name;
        this.properties = new TreeSet<>(properties);
    }

    public Name name() {
        return name;
    }

    public Set<Signature> properties() {
        return properties;
    }
}
