package cz.matfyz.server.job;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.job.jobpayload.JobPayload;
import cz.matfyz.server.utils.DatabaseWrapper;
import cz.matfyz.server.utils.entity.Id;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cz.matfyz.server.utils.Utils.*;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JobRepository {

    @Autowired
    private DatabaseWrapper db;

    public record JobWithRun(Job job, Run run) {}

    private static JobWithRun jobWithRunFromResultSet(ResultSet resultSet, Id jobId) throws SQLException, JsonProcessingException {
        final String jsonValue = resultSet.getString("job.json_value");
        final var run = runFromResultSet(resultSet);

        return new JobWithRun(
            Job.fromJsonValue(jobId, run.id(), jsonValue),
            run
        );
    }

    public record RunWithJobs(Run run, List<JobInfo> jobs) {}

    public record JobInfo(Id id, int index, String label, Date createdAt, JobPayload payload, Job.State state) {
        public static JobInfo fromJob(Job job) {
            return new JobInfo(
                job.id(),
                job.index,
                job.label,
                job.createdAt,
                job.payload,
                job.state
            );
        }
    }

    private static Run runFromResultSet(ResultSet resultSet) throws SQLException {
        final Id runId = getId(resultSet, "run.id");
        final Id categoryId = getId(resultSet, "run.category_id");
        final String runLabel = resultSet.getString("run.label");
        final @Nullable Id actionId = getId(resultSet, "run.action_id");
        final @Nullable Id sessionId = getId(resultSet, "run.session_id");

        return Run.fromDatabase(runId, categoryId, runLabel, actionId, sessionId);
    }

    private static JobInfo jobInfoFromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        final Id jobId = getId(resultSet, "job.id");
        final int index = resultSet.getInt("job.index");
        final String label = resultSet.getString("job.label");
        final Date createdAt = new Date(resultSet.getLong("job.created_at"));
        final String payload = resultSet.getString("job.payload");
        final Job.State state = Job.State.valueOf(resultSet.getString("job.state"));

        return new JobInfo(jobId, index, label, createdAt, JobPayload.fromJsonValue(payload), state);
    }

    public List<JobWithRun> findAllInCategory(Id categoryId, Id sessionId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    job.id as "job.id",
                    job.json_value as "job.json_value",
                    run.id as "run.id",
                    run.category_id as "run.category_id",
                    run.label as "run.label",
                    run.action_id as "run.action_id",
                    run.session_id as "run.session_id"
                FROM job
                JOIN run ON run.id = job.run_id
                WHERE run.category_id = ?
                    AND (run.session_id = ? OR run.session_id IS NULL)
                ORDER BY job.id;
                """);
            setId(statement, 1, categoryId);
            setId(statement, 2, sessionId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id jobId = getId(resultSet, "job.id");
                output.add(jobWithRunFromResultSet(resultSet, jobId));
            }
        });
    }

    public List<Id> findAllActiveRunIds() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT DISTINCT run.id as "run.id"
                FROM run
                WHERE run.is_active = TRUE
                ORDER BY run.id;
                """);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id runId = getId(resultSet, "run.id");
                output.add(runId);
            }
        });
    }

    public JobWithRun find(Id jobId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    job.json_value as "job.json_value",
                    run.id as "run.id",
                    run.label as "run.label",
                    run.category_id as "run.category_id",
                    run.action_id as "run.action_id",
                    run.session_id as "run.session_id"
                FROM job
                JOIN run ON run.id = job.run_id
                WHERE job.id = ?;
                """);
            setId(statement, 1, jobId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                output.set(jobWithRunFromResultSet(resultSet, jobId));
            }
        });
    }

    public RunWithJobs findRunWithJobs(Id runId) {
        return db.get((connection, output) -> {
            // We use a simple join because we want only runds with jobs.
            // The other shouldn't even exist, but just to be sure ...
            final var statement = connection.prepareStatement("""
                SELECT
                    run.id as "run.id",
                    run.category_id as "run.category_id",
                    run.label as "run.label",
                    run.action_id as "run.action_id",
                    run.session_id as "run.session_id",
                    job.id as "job.id",
                    job.json_value->>'index' as "job.index",
                    job.json_value->>'label' as "job.label",
                    job.json_value->>'createdAt' as "job.created_at",
                    job.json_value->>'payload' as "job.payload",
                    job.json_value->>'state' as "job.state"
                FROM run
                JOIN job ON job.run_id = run.id
                WHERE run.id = ?;
                """);
            setId(statement, 1, runId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var run = runFromResultSet(resultSet);
                final var jobs = new ArrayList<JobInfo>();
                do {
                    jobs.add(jobInfoFromResultSet(resultSet));
                } while (resultSet.next());

                output.set(new RunWithJobs(run, jobs));
            }
        });
    }

    public void save(Job job) {
        db.run(connection -> {
            // Whenever a job is changed, the run is marked active again.
            final var statement = connection.prepareStatement("""
                INSERT INTO job (id, run_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    run_id = EXCLUDED.run_id,
                    json_value = EXCLUDED.json_value;

                UPDATE run SET is_active = TRUE
                WHERE id = ?;
                """);
            setId(statement, 1, job.id());
            setId(statement, 2, job.runId);
            statement.setString(3, job.toJsonValue());
            setId(statement, 4, job.runId);
            executeChecked(statement);
        });
    }

    public void save(Run run) {
        db.run(connection -> {
            // Whenever a run is changed, it's also marked as active.
            final var statement = connection.prepareStatement("""
                INSERT INTO run (id, category_id, label, action_id, session_id, is_active)
                VALUES (?, ?, ?, ?, ?, TRUE)
                ON CONFLICT (id) DO UPDATE SET
                    category_id = EXCLUDED.category_id,
                    action_id = EXCLUDED.action_id,
                    session_id = EXCLUDED.session_id,
                    is_active = EXCLUDED.is_active;
                """);
            setId(statement, 1, run.id());
            setId(statement, 2, run.categoryId);
            statement.setString(3, run.label);
            setId(statement, 4, run.actionId);
            setId(statement, 5, run.sessionId);
            executeChecked(statement);
        });
    }

    public void deactivateRun(Id runId) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                UPDATE run SET is_active = FALSE
                WHERE id = ?;
                """);
            setId(statement, 1, runId);
            executeChecked(statement);
        });
    }

    public List<Session> findAllSessionsInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id, category_id, json_value
                FROM session
                WHERE category_id = ?
                ORDER BY session.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id id = getId(resultSet, "id");
                final String jsonValue = resultSet.getString("json_value");
                output.add(Session.fromJsonValue(id, categoryId, jsonValue));
            }
        });
    }

    public void save(Session session) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO session (id, category_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    category_id = EXCLUDED.category_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, session.id());
            setId(statement, 2, session.categoryId);
            statement.setString(3, session.toJsonValue());
            executeChecked(statement);
        });
    }

}
