package cz.matfyz.server.exception;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job.State;

import java.io.Serializable;

public class InvalidTransitionException extends ServerException {

    private record Data(
        Id id,
        Serializable prevState,
        Serializable nextState
    ) implements Serializable {}

    private InvalidTransitionException(String name, Data data) {
        super("invalidTransitionException." + name, data, null);
    }

    public static InvalidTransitionException job(Id id, State prevState, State nextState) {
        return new InvalidTransitionException("job", new Data(id, prevState, nextState));
    }

}
