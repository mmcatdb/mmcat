package cz.matfyz.server.exception;

import cz.matfyz.server.entity.Id;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class SessionException extends ServerException {

    private SessionException(String name, Serializable data) {
        super("session." + name, null, null);
    }

    private record JobData(
        Id id
    ) implements Serializable {}

    public static SessionException notFound(Id jobId) {
        return new SessionException("notFound", new JobData(jobId));
    }

}
