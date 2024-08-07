package cz.matfyz.server.entity;

import cz.matfyz.core.identifiers.Identified;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Entity implements Identified<Entity, Id> {

    public final @Nullable Id id;

    public Entity(Id id) {
        this.id = id;
    }

    @Override public @Nullable Id identifier() {
        return id;
    }

    @Override public boolean equals(@Nullable Object obj) {
        return obj instanceof Entity entity && id != null && id.equals(entity.id);
    }

}
