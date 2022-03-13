package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.server.entity.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

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
public class SchemaMorphismRepository
{
    public List<SchemaMorphismWrapper> findAllInCategory(int categoryId)
    {
        var output = new ArrayList<SchemaMorphismWrapper>();

        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_morphism
                JOIN schema_morphism_in_category ON (schema_morphism_id = schema_morphism.id)
                WHERE schema_category_id = ?;
            """);
            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();

            //var builder = new SchemaCategory.Builder();
            while (resultSet.next())
            {
                var id = resultSet.getInt("id");
                //var jsonValue = new JSONObject(resultSet.getString("json_value"));
                var domId = resultSet.getInt("domain_object_id");
                var codId = resultSet.getInt("codomain_object_id");
                var jsonValue = resultSet.getString("json_value");
                //var schema = builder.fromJSON(jsonObject);
                output.add(new SchemaMorphismWrapper(id, domId, codId, jsonValue));
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return output;
    }

    public SchemaMorphismWrapper find(int id)
    {
        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("SELECT * FROM schema_morphism WHERE id = ?;");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                //var jsonValue = new JSONObject(resultSet.getString("json_value"));
                var domId = resultSet.getInt("domain_object_id");
                var codId = resultSet.getInt("codomain_object_id");
                var jsonValue = resultSet.getString("json_value");
                //var schema = new SchemaCategory.Builder().fromJSON(jsonObject);
                return new SchemaMorphismWrapper(id, domId, codId, jsonValue);
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return null;
    }

    public Integer add(SchemaMorphism morphism)
    {
        Connection connection = null;
        try
        {
            /*
            connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("INSERT INTO schema_category (json_value) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, schema.toJSON().toString());
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0)
                throw new SQLException("Create new schema category failed, no rows affected.");

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                return Integer.toString(generatedKeys.getInt("id"));
                */
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
