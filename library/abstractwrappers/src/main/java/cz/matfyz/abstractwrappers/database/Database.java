package cz.matfyz.abstractwrappers.database;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.core.mapping.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a database. It's identified by an identifier (which doesn't have to be anything specific - it's there just for the Comparable interface).
 * It has a type (PostgreSQL, MongoDB, ...) and a list of kinds.
 * Most importantly, it provides a direct access to the database through the control wrapper.
 */
public class Database implements Comparable<Database> {

    public enum DatabaseType {
        mongodb,
        postgresql,
        neo4j
    }

    public final DatabaseType type;
    public final AbstractControlWrapper control;
    public final String identifier;
    // The set ensures there is at most one mapping for each kindName in the database.
    public final Set<Kind> kinds;

    private Database(DatabaseType type, AbstractControlWrapper control, String identifier, Set<Kind> kinds) {
        this.type = type;
        this.control = control;
        this.identifier = identifier;
        this.kinds = kinds;
    }

    @Override public int compareTo(Database other) {
        return identifier.compareTo(other.identifier);
    }

    @Override public boolean equals(Object object) {
        return object instanceof Database other && compareTo(other) == 0;
    }

    public static class Builder {

        private List<Mapping> mappings = new ArrayList<>();

        public Builder mapping(Mapping mapping) {
            this.mappings.add(mapping);

            return this;
        }

        public Database build(DatabaseType type, AbstractControlWrapper control, String identifier) {
            final var database = new Database(type, control, identifier, new TreeSet<>());
            mappings.forEach(m -> database.kinds.add(new Kind(m, database)));

            return database;
        }

    }

}
