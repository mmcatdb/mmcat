package cz.cuni.matfyz.core.schema;

import java.util.*;

/**
 * Superid is a set of attributes (each corresponding to a signature of a base or composite morphism) forming the actual data contents a given object is expected to have.
 * @author jachymb.bartik
 */
public class SuperId implements Comparable<SuperId>
{
    private final Set<Property> ids;

	public SuperId(Set<Property> ids)
    {
		this.ids = ids;
	}

	public SuperId(Property... ids)
    {
		this.ids = new TreeSet(List.of(ids));
	}

	@Override
	public int compareTo(SuperId superid)
    {
        throw new UnsupportedOperationException();
	}
}
