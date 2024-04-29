package cz.matfyz.abstractwrappers.datasource;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.core.mapping.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a database or a file. It's identified by an identifier (which doesn't have to be anything specific - it's there just for the Comparable interface).
 * It has a type (PostgreSQL, MongoDB, ...) and a list of kinds.
 * Most importantly, it provides a direct access to the database through the control wrapper.
 */
public class Datasource implements Comparable<Datasource> {

    public enum DatasourceType {
        mongodb,
        postgresql,
        neo4j,
        csv,
        json,
        jsonld,
    }

    public final DatasourceType type;
    public final AbstractControlWrapper control;
    public final String identifier;
    // The set ensures there is at most one mapping for each kindName in the datasource.
    public final Set<Kind> kinds;

    private Datasource(DatasourceType type, AbstractControlWrapper control, String identifier, Set<Kind> kinds) {
        this.type = type;
        this.control = control;
        this.identifier = identifier;
        this.kinds = kinds;
    }

    @Override public int compareTo(Datasource other) {
        return identifier.compareTo(other.identifier);
    }

    @Override public boolean equals(Object object) {
        return object instanceof Datasource other && compareTo(other) == 0;
    }

    public static class Builder {

        private List<Mapping> mappings = new ArrayList<>();

        public Builder mapping(Mapping mapping) {
            this.mappings.add(mapping);

            return this;
        }

        public Datasource build(DatasourceType type, AbstractControlWrapper control, String identifier) {
            final var datasource = new Datasource(type, control, identifier, new TreeSet<>());
            mappings.forEach(m -> datasource.kinds.add(new Kind(m, datasource)));

            return datasource;
        }

    }

}

