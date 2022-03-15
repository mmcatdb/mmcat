package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.server.entity.SchemaCategoryInfo;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Connection;
import java.util.*;

import org.springframework.stereotype.Repository;



/**
 * 
 * @author jachym.bartik
 */
@Repository
public class SchemaCategoryRepository {

    public List<SchemaCategoryInfo> findAll() {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT * FROM schema_category;");

            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                //var jsonValue = new JSONObject(resultSet.getString("json_value"));
                var jsonValue = resultSet.getString("json_value");
                output.add(new SchemaCategoryInfo(id, jsonValue));
            }
        });
    }

    public SchemaCategoryInfo find(int id) {
        return DatabaseWrapper.get((connection, output) -> {

            var statement = connection.prepareStatement("SELECT * FROM schema_category WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                var jsonValue = resultSet.getString("json_value");
                output.set(new SchemaCategoryInfo(id, jsonValue));
            }
        });
    }

    public Integer add(SchemaCategory schema) {
        // TODO
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
