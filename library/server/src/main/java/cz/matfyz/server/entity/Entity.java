package cz.matfyz.server.entity;

import cz.matfyz.core.identifiers.Identified;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Entity implements Identified<Entity, Id> {

    private Id id;

    public Entity(Id id) {
        this.id = id;
    }

    @JsonProperty("id")
    public Id id() {
        return id;
    }

    @Override public Id identifier() {
        return id();
    }

    @Override public boolean equals(@Nullable Object obj) {
        return obj instanceof Entity entity && id != null && id.equals(entity.id);
    }

    @Override public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

}
