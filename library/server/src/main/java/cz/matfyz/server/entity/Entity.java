package cz.matfyz.server.entity;

import cz.matfyz.core.identifiers.Identified;

/**
 * @author jachym.bartik
 */
public class Entity implements Identified<Entity, Id> {

    public final Id id;

    public Entity(Id id) {
        this.id = id;
    }

    @Override public Id identifier() {
        return id;
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof Entity entity && id.equals(entity.id);
    }

}
