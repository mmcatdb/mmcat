package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.job.Job;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.repository.utils.Utils;

import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class JobRepository {

    private static final ObjectWriter jobJsonWriter = new ObjectMapper().writer();

    @Autowired
    private DatabaseWrapper db;

    public List<Job> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM job
                WHERE schema_category_id = ?
                ORDER BY job.id;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                Id logicalModelId = getId(resultSet, "logical_model_id");
                Id dataSourceId = getId(resultSet, "data_source_id");
                String jsonValue = resultSet.getString("json_value");
                output.add(new Job.Builder().fromJsonValue(id, categoryId, logicalModelId, dataSourceId, jsonValue));
            }
        });
    }

    public Job find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM job
                WHERE job.id = ?;
                """);
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Id categoryId = getId(resultSet, "schema_category_id");
                Id logicalModelId = getId(resultSet, "logical_model_id");
                Id dataSourceId = getId(resultSet, "data_source_id");
                String jsonValue = resultSet.getString("json_value");
                output.set(new Job.Builder().fromJsonValue(id, categoryId, logicalModelId, dataSourceId, jsonValue));
            }
        });
    }

    public Id add(Job job) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO job (schema_category_id, logical_model_id, data_source_id, json_value) VALUES (?, ?, ?, ?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            setId(statement, 1, job.categoryId);
            setId(statement, 2, job.logicalModelId);
            setId(statement, 3, job.dataSourceId);
            statement.setString(4, Utils.toJson(job));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

    public boolean updateJsonValue(Job job) {
        return db.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("""
                UPDATE job
                SET json_value = ?::jsonb
                WHERE id = ?;
                """);
            statement.setString(1, Utils.toJson(job));
            setId(statement, 2, job.id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

    public boolean delete(Id id) {
        return db.getBoolean((connection, output) -> {
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
