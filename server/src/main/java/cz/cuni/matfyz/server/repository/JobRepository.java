package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.entity.JobData;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.springframework.stereotype.Repository;


/**
 * 
 * @author jachym.bartik
 */
@Repository
public class JobRepository
{
    public List<Job> findAll()
    {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM job;");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int mappingId = resultSet.getInt("mapping_id");
                String jsonValue = resultSet.getString("json_value");
                output.add(new Job(id, mappingId, jsonValue));
            }
        });
    }

    public Job find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM job WHERE id = ?;");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                int mappingId = resultSet.getInt("mapping_id");
                String jsonValue = resultSet.getString("json_value");
                output.set(new Job(foundId, mappingId, jsonValue));
            }
        });
    }

    public Integer add(JobData jobData)
    {
        Connection connection = null;
        try
        {
            connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("INSERT INTO job (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS); // TODO
            statement.setString(1, jobData.value);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0)
                throw new SQLException("Create new job failed, no rows affected.");

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                return generatedKeys.getInt("id");
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }
        finally
        {
            try
            {
                if (connection != null)
                    connection.close();
            }
            catch(Exception e)
            {

            }
        }

        return null;
    }
}
