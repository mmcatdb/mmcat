package cz.cuni.matfyz.server.entity;

import cz.cuni.matfyz.core.serialization.Identified;

/**
 * 
 * @author jachym.bartik
 */
public class Entity implements Identified<Integer> {
    
    public final Integer id;

    public Entity(Integer id) {
        this.id = id;
    }

    @Override
    public Integer identifier() {
        return id;
    }

}
