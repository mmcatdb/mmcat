package cz.matfyz.server.exception;

import cz.matfyz.server.entity.Id;

import java.io.Serializable;

public class SessionException extends ServerException {

    private SessionException(String name, Serializable data) {
        super("session." + name, null, null);
    }

    public static SessionException notSet() {
        return new SessionException("notSet", null);
    }

    private record RunData(
        Id runId
    ) implements Serializable {}

    public static SessionException runNotInSession(Id runId) {
        return new SessionException("runNotInSession", new RunData(runId));
    }

}
