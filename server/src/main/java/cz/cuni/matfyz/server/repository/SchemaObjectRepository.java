package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.server.entity.Position;
import cz.cuni.matfyz.server.entity.SchemaObjectWrapper;
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
public class SchemaObjectRepository
{
    public List<SchemaObjectWrapper> findAllInCategory(int categoryId)
    {
        var output = new ArrayList<SchemaObjectWrapper>();

        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_object
                JOIN schema_object_in_category ON (schema_object_id = schema_object.id)
                WHERE schema_category_id = ?;
            """);
            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();

            //var builder = new SchemaObject.Builder();
            while (resultSet.next())
            {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                String jsonObject = resultSet.getString("json_value");
                JSONObject positionJsonObject = new JSONObject(resultSet.getString("position"));
                Position position = new Position.Builder().fromJSON(positionJsonObject);
                //var schema = builder.fromJSON(jsonObject);
                output.add(new SchemaObjectWrapper(resultSet.getInt("id"), jsonObject, position));
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return output;
    }

    public SchemaObjectWrapper find(int id)
    {
        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("SELECT * FROM schema_object WHERE id = ?;");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                var jsonObject = resultSet.getString("json_value");
                //var schema = new SchemaObject.Builder().fromJSON(jsonObject);
                return new SchemaObjectWrapper(resultSet.getInt("id"), jsonObject, null);
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return null;
    }

    public boolean updatePosition(int categoryId, int objectId, Position newPosition)
    {
        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("""
                UPDATE schema_object_in_category
                SET position = ?::jsonb
                WHERE schema_category_id = ?
                    AND schema_object_id = ?;
            """);
            statement.setString(1, newPosition.toJSON().toString());
            statement.setInt(2, categoryId);
            statement.setInt(3, objectId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0)
                throw new SQLException("Update position failed, no rows affected.");

            return true;
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return false;
    }

    /*
    public String add(SchemaObject object)
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
    */
}
