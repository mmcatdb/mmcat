package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.job.Session;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JobRepository {

    @Autowired
    private DatabaseWrapper db;

    public record JobWithRun(Job job, Run run) {}

    private static JobWithRun jobWithRunFromResultSet(ResultSet resultSet, Id jobId, Id categoryId) throws SQLException, JsonProcessingException {
        final Id runId = getId(resultSet, "run.id");
        final String jsonValue = resultSet.getString("job.json_value");
        final @Nullable Id actionId = getIdOrNull(resultSet, "run.action_id");
        final @Nullable Id sessionId = getIdOrNull(resultSet, "run.session_id");

        return new JobWithRun(
            Job.fromJsonValue(jobId, runId, jsonValue),
            Run.fromDatabase(runId, categoryId, actionId, sessionId)
        );
    }

    public List<JobWithRun> findAllInCategory(Id categoryId, Id sessionId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    job.id as "job.id",
                    job.json_value as "job.json_value",
                    run.id as "run.id",
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
                output.add(jobWithRunFromResultSet(resultSet, jobId, categoryId));
            }
        });
    }

    public List<Id> findAllReadyIds() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT job.id as "job.id"
                FROM job
                WHERE job.json_value->>'state' = ?
                ORDER BY job.id;
                """);
            statement.setString(1, Job.State.Ready.name());
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id jobId = getId(resultSet, "job.id");
                output.add(jobId);
            }
        });
    }

    public JobWithRun find(Id jobId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    job.json_value as "job.json_value",
                    run.id as "run.id",
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
                final Id categoryId = getId(resultSet, "run.category_id");
                output.set(jobWithRunFromResultSet(resultSet, jobId, categoryId));
            }
        });
    }

    public void save(Job job) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO job (id, run_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    run_id = EXCLUDED.run_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, job.id());
            setId(statement, 2, job.runId);
            statement.setString(3, job.toJsonValue());
            executeChecked(statement);
        });
    }

    public void save(Run run) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO run (id, category_id, action_id, session_id)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    category_id = EXCLUDED.category_id,
                    action_id = EXCLUDED.action_id,
                    session_id = EXCLUDED.session_id;
                """);
            setId(statement, 1, run.id());
            setId(statement, 2, run.categoryId);
            setId(statement, 3, run.actionId);
            setId(statement, 4, run.sessionId);
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
