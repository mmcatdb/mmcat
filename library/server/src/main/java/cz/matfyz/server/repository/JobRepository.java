package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.job.Session;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class JobRepository {

    @Autowired
    private DatabaseWrapper db;

    public record JobWithRun(Job job, Run run) {}

    private static JobWithRun jobWithRunFromResultSet(ResultSet resultSet, Id jobId, Id categoryId) throws SQLException, JsonProcessingException {
        final Id runId = getId(resultSet, "run.id");
        final Id actionId = getId(resultSet, "run.action_id");
        final String jsonValue = resultSet.getString("job.json_value");

        return new JobWithRun(
            Job.fromJsonValue(jobId, runId, jsonValue),
            Run.fromDatabase(runId, categoryId, actionId)
        );
    }

    public List<JobWithRun> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    job.id as "job.id",
                    job.json_value as "job.json_value",
                    run.id as "run.id",
                    run.action_id as "run.action_id"
                FROM job
                JOIN run ON run.id = job.run_id
                WHERE run.schema_category_id = ?
                ORDER BY job.id;
                """);
            setId(statement, 1, categoryId);
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
                    run.schema_category_id as "run.schema_category_id",
                    run.action_id as "run.action_id"
                FROM job
                JOIN run ON run.id = job.run_id
                WHERE job.id = ?;
                """);
            setId(statement, 1, jobId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final Id categoryId = getId(resultSet, "run.schema_category_id");
                output.set(jobWithRunFromResultSet(resultSet, jobId, categoryId));
            }
        });
    }

    public boolean save(Job job) {
        return db.getBoolean((connection, output) -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO job (id, run_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    run_id = EXCLUDED.run_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, job.id);
            setId(statement, 2, job.runId);
            statement.setString(3, job.toJsonValue());

            output.set(statement.executeUpdate() != 0);
        });
    }

    public boolean save(Run run) {
        return db.getBoolean((connection, output) -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO run (id, schema_category_id, action_id)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    schema_category_id = EXCLUDED.schema_category_id,
                    action_id = EXCLUDED.action_id;
                """);
            setId(statement, 1, run.id);
            setId(statement, 2, run.categoryId);
            setId(statement, 3, run.actionId, true);

            output.set(statement.executeUpdate() != 0);
        });
    }

    public List<Session> findAllSessionsInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id, schema_category_id, json_value
                FROM session
                WHERE schema_category_id = ?
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

    public boolean save(Session session) {
        return db.getBoolean((connection, output) -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO session (id, schema_category_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    schema_category_id = EXCLUDED.schema_category_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, session.id);
            setId(statement, 2, session.categoryId);
            statement.setString(3, session.toJsonValue());

            output.set(statement.executeUpdate() != 0);
        });
    }

    public String findInstanceCategoryJson(Id sessionId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    instance_data
                FROM session
                WHERE id = ?;
                """);
            setId(statement, 1, sessionId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                output.set(resultSet.getString("instance_data"));
            }
        });
    }

    public boolean saveInstanceCategoryJson(Id sessionId, String json) {
        return db.getBoolean((connection, output) -> {
            final var statement = connection.prepareStatement("""
                UPDATE session
                SET instance_data = ?
                WHERE id = ?;
                """);
            statement.setString(1, json);
            setId(statement, 2, sessionId);

            output.set(statement.executeUpdate() != 0);
        });
    }

}
