package cz.matfyz.server.exception;

import cz.matfyz.server.entity.Id;

import java.io.Serializable;

public class EntityException extends ServerException {

    private EntityException(String name, Serializable data) {
        super("entity." + name, data, null);
    }

    public static EntityException reassignId(Id id) {
        return new EntityException("reassignId", id.toString());
    }

    public static EntityException readNullId() {
        return new EntityException("readNullId", null);
    }

    public static EntityException writeNullId() {
        return new EntityException("writeNullId", null);
    }

}
