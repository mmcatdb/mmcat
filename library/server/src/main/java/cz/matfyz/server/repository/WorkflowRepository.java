package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.workflow.Workflow;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WorkflowRepository {

    @Autowired
    private DatabaseWrapper db;

    private Workflow fromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        final var id = getId(resultSet, "id");
        final var categoryId = getId(resultSet, "category_id");
        final var label = resultSet.getString("label");
        final var jobId = getId(resultSet, "job_id");
        final var jsonValue = resultSet.getString("json_value");

        return Workflow.fromJsonValue(id, categoryId, label, jobId, jsonValue);
    }

    public @Nullable Workflow find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    category_id,
                    label,
                    job_id,
                    json_value
                FROM workflow
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        });
    }

    public List<Workflow> findAll() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    category_id,
                    label,
                    job_id,
                    json_value
                FROM workflow
                ORDER BY id;
                """);
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public void save(Workflow workflow) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO workflow (id, category_id, label, job_id, json_value)
                VALUES (?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    category_id = EXCLUDED.category_id,
                    label = EXCLUDED.label,
                    job_id = EXCLUDED.job_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, workflow.id());
            setId(statement, 2, workflow.categoryId);
            statement.setString(3, workflow.label);
            setId(statement, 4, workflow.jobId);
            statement.setString(5, workflow.toJsonValue());
            executeChecked(statement);
        });
    }

}
