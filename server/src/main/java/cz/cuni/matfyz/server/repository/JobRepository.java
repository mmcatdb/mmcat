package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Job;

import java.sql.ResultSet;
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
}
