package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.MappingWrapper;

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
public class MappingRepository
{
    public List<MappingWrapper> findAllInCategory(int categoryId)
    {
        var output = new ArrayList<MappingWrapper>();

        /*
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

            //var builder = new Mapping.Builder();
            while (resultSet.next())
            {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                String jsonObject = resultSet.getString("json_value");
                JSONObject positionJsonObject = new JSONObject(resultSet.getString("position"));
                Position position = new Position.Builder().fromJSON(positionJsonObject);
                //var schema = builder.fromJSON(jsonObject);
                output.add(new MappingWrapper(resultSet.getInt("id"), jsonObject, position));
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }
        */

        return output;
    }

    public MappingWrapper find(int id)
    {
        /*
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
                //var schema = new Mapping.Builder().fromJSON(jsonObject);
                return new MappingWrapper(resultSet.getInt("id"), jsonObject, null);
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }
        */

        return null;
    }
}
