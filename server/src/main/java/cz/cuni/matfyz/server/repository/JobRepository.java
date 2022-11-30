package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class JobRepository {

    public List<Job> findAllInCategory(int categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    job.id as _id,
                    mapping.id as _mapping_id,
                    job.json_value as _json_value
                FROM job
                JOIN mapping ON mapping.id = job.mapping_id
                JOIN logical_model on logical_model.id = mapping.logical_model_id
                WHERE logical_model.schema_category_id = ?
                ORDER BY job.id;
                """);
            statement.setInt(1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("_id");
                int mappingId = resultSet.getInt("_mapping_id");
                String jsonValue = resultSet.getString("_json_value");
                output.add(new Job.Builder().fromJSON(id, mappingId, categoryId, jsonValue));
            }
        });
    }

    public Job find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    job.id as _id,
                    mapping.id as _mapping_id,
                    job.json_value as _json_value,
                    logical_model.schema_category_id as _schema_category_id
                FROM job
                JOIN mapping ON mapping.id = job.mapping_id
                JOIN logical_model on logical_model.id = mapping.logical_model_id
                WHERE job.id = ?
                ORDER BY job.id;
                """);
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int mappingId = resultSet.getInt("_mapping_id");
                int categoryId = resultSet.getInt("_schema_category_id");
                String jsonValue = resultSet.getString("_json_value");
                output.set(new Job.Builder().fromJSON(id, mappingId, categoryId, jsonValue));
            }
        });
    }

    public Integer add(Job job) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO job (mapping_id, json_value) VALUES (?, ?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, job.mappingId);
            statement.setString(2, job.toJSON().toString());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(generatedKeys.getInt("id"));
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
            statement.setInt(2, job.id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

    public boolean delete(Integer id) {
        return DatabaseWrapper.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("""
                DELETE FROM job
                WHERE id = ?;
                """);
            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

}
