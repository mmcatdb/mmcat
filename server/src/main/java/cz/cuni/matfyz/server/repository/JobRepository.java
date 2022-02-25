package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.entity.JobData;

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
        var output = new ArrayList<Job>();

        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM job;");

            while (resultSet.next())
            {
                String id = Integer.toString(resultSet.getInt("id"));
                String content = resultSet.getString("content");
                output.add(new Job(id, content));
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return output;
    }

    public Job find(String id)
    {
        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("SELECT * FROM job WHERE id = ?;");
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                String foundId = Integer.toString(resultSet.getInt("id"));
                String content = resultSet.getString("content");
                return new Job(foundId, content);
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return null;
    }

    public String add(JobData jobData)
    {
        Connection connection = null;
        try
        {
            connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("INSERT INTO job(value) VALUES(?);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, jobData.value);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0)
                throw new SQLException("Create new job failed, no rows affected.");

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                return Integer.toString(generatedKeys.getInt("id"));
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
