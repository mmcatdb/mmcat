package cz.matfyz.abstractwrappers.database;

import cz.matfyz.core.mapping.Mapping;

/**
 * This class represents a kind in a database. It's identified by the databaseId and mapping (more precisely, by the name of the collection, i.e., kindName).
 */
public class Kind implements Comparable<Kind> {

    public final Database database;
    public final Mapping mapping;

    public Kind(Mapping mapping, Database database) {
        this.mapping = mapping;
        this.database = database;
    }

    @Override
    public int compareTo(Kind other) {
        final int databaseComparison = database.compareTo(other.database);
        return databaseComparison != 0
            ? databaseComparison
            : mapping.compareTo(other.mapping);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Kind other && compareTo(other) == 0;
    }

    /*
     * Creates a new kind with the given mapping and replaces the old one in the database.
     */
    public Kind updateMapping(Mapping mapping) {
        final var newKind = new Kind(mapping, database);
        final int oldIndex = database.kinds.indexOf(this);
        database.kinds.set(oldIndex, newKind);

        return newKind;
    }

}