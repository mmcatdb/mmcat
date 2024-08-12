package cz.matfyz.server.entity;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.server.exception.EntityException;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Entity implements Identified<Entity, Id> {

    private @Nullable Id id;

    public Entity(@Nullable Id id) {
        this.id = id;
    }

    @JsonProperty("id")
    public Id id() {
        if (id == null)
            throw EntityException.readNullId();

        return id;
    }

    public void assignId(Id id) {
        if (this.id != null)
            throw EntityException.reassignId(this.id);

        // This shouldn't be necessary because it should be covered by the @Nullable check. However, the checker isn't propertly implemented yet, so ...
        if (id == null)
            throw EntityException.writeNullId();

        this.id = id;
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
