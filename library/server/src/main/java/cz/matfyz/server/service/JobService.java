package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.ActionPayload;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.job.Session;
import cz.matfyz.server.entity.job.Job.State;
import cz.matfyz.server.exception.InvalidTransitionException;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class JobService {

    @Autowired
    private JobRepository repository;

    public JobWithRun createRun(Action action) {
        return createRun(action.categoryId, action.id, action.label, action.payload);
    }

    public JobWithRun createRun(Id categoryId, @Nullable Id actionId, String label, ActionPayload payload) {
        final var run = Run.createNew(categoryId, actionId);
        final var job = Job.createNew(run.id, label, payload, isJobStartedManually(payload), null);

        repository.save(run);
        repository.save(job);

        return new JobWithRun(job, run);
    }

    private boolean isJobStartedManually(ActionPayload payload) {
        // return payload instanceof UpdateSchemaPayload;
        return true;
    }

    public JobWithRun createRestartedJob(JobWithRun jobWithRun, @Nullable Id sessionId) {
        final var job = jobWithRun.job();
        final var newJob = Job.createNew(job.runId, job.label, job.payload, false, sessionId);

        repository.save(newJob);

        return new JobWithRun(newJob, jobWithRun.run());
    }

    public JobWithRun transition(JobWithRun jobWithRun, State newState, @Nullable Id sessionId) {
        final var job = jobWithRun.job();
        final State prevState = job.state;
        if (!allowedTransitions.containsKey(newState) || !allowedTransitions.get(newState).contains(prevState))
            throw InvalidTransitionException.job(job.id, prevState, newState);

        job.state = newState;
        job.sessionId = sessionId;
        repository.save(job);

        return new JobWithRun(job, jobWithRun.run());
    }

    private static final Map<State, Set<State>> allowedTransitions = defineAllowedTransitions();

    private static Map<State, Set<State>> defineAllowedTransitions() {
        final var output = new TreeMap<State, Set<State>>();
        output.put(State.Paused, Set.of(State.Ready));
        output.put(State.Ready, Set.of(State.Paused));
        output.put(State.Canceled, Set.of(State.Paused, State.Ready));

        return output;
    }

    public List<Session> findAllSessions(Id categoryId) {
        return repository.findAllSessionsInCategory(categoryId);
    }

    public Session createSession(Id categoryId) {
        final var session = Session.createNew(categoryId);
        repository.save(session);

        return session;
    }

}
