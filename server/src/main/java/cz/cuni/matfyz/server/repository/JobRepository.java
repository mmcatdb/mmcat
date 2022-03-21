package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.*;
import org.springframework.stereotype.Repository;


/**
 * 
 * @author jachym.bartik
 */
@Repository
public class JobRepository {

    public List<Job> findAll() {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT * FROM job ORDER BY id;");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int mappingId = resultSet.getInt("mapping_id");
                String jsonValue = resultSet.getString("json_value");
                output.add(new Job.Builder().fromJSON(id, mappingId, jsonValue));
            }
        });
    }

    public Job find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM job WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int mappingId = resultSet.getInt("mapping_id");
                String jsonValue = resultSet.getString("json_value");
                output.set(new Job.Builder().fromJSON(id, mappingId, jsonValue));
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

}
