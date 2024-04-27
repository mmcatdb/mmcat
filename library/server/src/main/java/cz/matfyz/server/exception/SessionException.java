package cz.matfyz.server.exception;

import cz.matfyz.server.entity.Id;

import java.io.Serializable;

public class SessionException extends ServerException {

    private SessionException(String name, Serializable data) {
        super("session." + name, null, null);
    }

    private record RunData(
        Id id
    ) implements Serializable {}

    public static SessionException notFound(Id runId) {
        return new SessionException("notFound", new RunData(runId));
    }

}
