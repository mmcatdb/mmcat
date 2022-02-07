package cz.cuni.matfyz.core.mapping;

import java.util.*;

public class Reference
{
    private final Name name;
    private final Set<AccessPath> properties;

    public Reference(Name name, Collection<AccessPath> properties)
    {
        this.name = name;
        this.properties = new TreeSet<>(properties);
    }

    public Name name()
    {
        return name;
    }

    public Set<AccessPath> properties()
    {
        return properties;
    }
}
