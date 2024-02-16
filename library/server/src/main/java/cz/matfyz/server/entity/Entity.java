package cz.matfyz.server.entity;

import cz.matfyz.core.identification.Identified;

/**
 * @author jachym.bartik
 */
public class Entity implements Identified<Id> {
    
    public final Id id;

    public Entity(Id id) {
        this.id = id;
    }

    @Override public Id identifier() {
        return id;
    }

}
