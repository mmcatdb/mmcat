package cz.matfyz.core.datasource;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a database or a file. It's identified by an identifier (which doesn't have to be anything specific - it's here just for the Comparable interface).
 */
public class Datasource implements Comparable<Datasource>, Serializable {

    public enum DatasourceType {
        mongodb,
        postgresql,
        neo4j,
        csv,
        json,
        jsonld,
    }

    public final DatasourceType type;
    public final String identifier;

    @JsonCreator
    public Datasource(
        @JsonProperty("type") DatasourceType type,
        @JsonProperty("identifier") String identifier
    ) {
        this.type = type;
        this.identifier = identifier;
    }

    /**
     * This identifier is unique among all kinds in all datasources.
     */
    public String getUniqueKindIdentifier(String kindName) {
        return identifier + "/" + kindName;
    }

    @Override public int compareTo(Datasource other) {
        return identifier.compareTo(other.identifier);
    }

    @Override public boolean equals(Object object) {
        return object instanceof Datasource other && compareTo(other) == 0;
    }

}

