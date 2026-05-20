package cz.matfyz.server.exception;

import cz.matfyz.server.job.Job.State;
import cz.matfyz.server.utils.entity.Id;

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
