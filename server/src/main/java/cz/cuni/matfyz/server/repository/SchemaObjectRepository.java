package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Position;
import cz.cuni.matfyz.server.entity.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.util.*;

import org.springframework.stereotype.Repository;

/**
 * 
 * @author jachym.bartik
 */
@Repository
public class SchemaObjectRepository {

    public List<SchemaObjectWrapper> findAllInCategory(int categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_object
                JOIN schema_object_in_category ON (schema_object_id = schema_object.id)
                WHERE schema_category_id = ?;
            """);
            statement.setInt(1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                String jsonObject = resultSet.getString("json_value");
                String jsonPosition = resultSet.getString("position");
                Position position = new Position.Builder().fromJSON(jsonPosition);
                //var schema = builder.fromJSON(jsonObject);

                if (position != null)
                    output.add(new SchemaObjectWrapper(resultSet.getInt("id"), jsonObject, position));
            }
        });
    }

    public SchemaObjectWrapper find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM schema_object WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                var jsonObject = resultSet.getString("json_value");
                //var schema = new SchemaObject.Builder().fromJSON(jsonObject);
                output.set(new SchemaObjectWrapper(resultSet.getInt("id"), jsonObject, null));
            }
        });
    }

    public boolean updatePosition(int categoryId, int objectId, Position newPosition) {
        return DatabaseWrapper.getBoolean((connection, output) -> {
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

            output.set(affectedRows > 0);
        });
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
