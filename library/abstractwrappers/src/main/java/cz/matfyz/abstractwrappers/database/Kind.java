package cz.matfyz.abstractwrappers.database;

import cz.matfyz.core.mapping.Mapping;

/**
 * This class represents a kind in a database. It's identified by the databaseId and mapping (more precisely, by the name of the collection, i.e., kindName).
 */
public class Kind implements Comparable<Kind> {

    public final Database database;
    /**
     * There has to be at most one mapping for each king name in a given database.
     * If not, many algorithms will broke.
     *
     * E.g., there is an algorithm for determining which kinds to join for a query. If we allowed multiple mappings for a single kind, we would have to use some other identifier (e.g., ID) to distinguish between them. However, the algorithm might then decide to join different mappings (i.e., with different IDs) for the same kind (one kindName). In that case, the joining would be much more complicated, because we can't join a kind with itself. We would need to merge the mappings of the kind together.
     *
     * But we could achieve that (and probably much more efficiently) by merging the mappings together before the algorithm starts. But then there is no point in allowing the "splitted" mappings in the first place.
     */
    public final Mapping mapping;

    public Kind(Mapping mapping, Database database) {
        this.mapping = mapping;
        this.database = database;
    }

    @Override public int compareTo(Kind other) {
        final int databaseComparison = database.compareTo(other.database);
        return databaseComparison != 0
            ? databaseComparison
            : mapping.compareTo(other.mapping);
    }

    @Override public boolean equals(Object object) {
        return object instanceof Kind other && compareTo(other) == 0;
    }

    @Override public String toString() {
        return database.identifier + "/" + mapping.kindName();
    }

}
