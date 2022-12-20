package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.job.Job;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class JobRepository {

    public List<Job> findAllInCategory(Id categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    job.id as _id,
                    job.logical_model_id as _logical_model_id,
                    job.json_value as _json_value
                FROM job
                JOIN logical_model on logical_model.id = job.logical_model_id
                WHERE logical_model.schema_category_id = ?
                ORDER BY job.id;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "_id");
                Id logicalModelId = getId(resultSet, "_logical_model_id");
                String jsonValue = resultSet.getString("_json_value");
                output.add(new Job.Builder().fromJSON(id, logicalModelId, categoryId, jsonValue));
            }
        });
    }

    public Job find(Id id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    job.id as _id,
                    job.logical_model_id as _logical_model_id,
                    job.json_value as _json_value,
                    logical_model.schema_category_id as _schema_category_id
                FROM job
                JOIN logical_model on logical_model.id = job.logical_model_id
                WHERE job.id = ?
                ORDER BY job.id;
                """);
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Id logicalModelId = getId(resultSet, "_logical_model_id");
                Id categoryId = getId(resultSet, "_schema_category_id");
                String jsonValue = resultSet.getString("_json_value");
                output.set(new Job.Builder().fromJSON(id, logicalModelId, categoryId, jsonValue));
            }
        });
    }

    public Id add(Job job) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO job (logical_model_id, json_value) VALUES (?, ?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            setId(statement, 1, job.logicalModelId);
            statement.setString(2, job.toJSON().toString());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

    public boolean updateJSONValue(Job job) {
        return DatabaseWrapper.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("""
                UPDATE job
                SET json_value = ?::jsonb
                WHERE id = ?;
                """);
            statement.setString(1, job.toJSON().toString());
            setId(statement, 2, job.id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

    public boolean delete(Id id) {
        return DatabaseWrapper.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("""
                DELETE FROM job
                WHERE id = ?;
                """);
            setId(statement, 1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

}
