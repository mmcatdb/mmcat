package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.server.entity.IdentifiedSchemaCategory;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;



/**
 * 
 * @author jachym.bartik
 */
@Repository
public class SchemaCategoryRepository
{
    public List<IdentifiedSchemaCategory> findAll()
    {
        var output = new ArrayList<IdentifiedSchemaCategory>();

        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM schema_category;");

            var builder = new SchemaCategory.Builder();
            while (resultSet.next())
            {
                var jsonObject = new JSONObject(resultSet.getString("json_value"));
                var schema = builder.fromJSON(jsonObject);
                output.add(new IdentifiedSchemaCategory(Integer.toString(resultSet.getInt("id")), schema));
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return output;
    }

    public IdentifiedSchemaCategory find(String id)
    {
        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("SELECT * FROM schema_category WHERE id = ?;");
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                var jsonObject = new JSONObject(resultSet.getString("json_value"));
                var schema = new SchemaCategory.Builder().fromJSON(jsonObject);
                return new IdentifiedSchemaCategory(Integer.toString(resultSet.getInt("id")), schema);
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return null;
    }

    public String add(SchemaCategory schema)
    {
        Connection connection = null;
        try
        {
            connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("INSERT INTO schema_category (json_value) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, schema.toJSON().toString());
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0)
                throw new SQLException("Create new schema category failed, no rows affected.");

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
