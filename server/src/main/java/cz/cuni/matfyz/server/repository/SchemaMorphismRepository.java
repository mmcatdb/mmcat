package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.server.entity.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Connection;
import java.util.*;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author jachym.bartik
 */
@Repository
public class SchemaMorphismRepository {
    
    public List<SchemaMorphismWrapper> findAllInCategory(int categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_morphism
                JOIN schema_morphism_in_category ON (schema_morphism_id = schema_morphism.id)
                WHERE schema_category_id = ?;
            """);
            statement.setInt(1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                //var jsonValue = new JSONObject(resultSet.getString("json_value"));
                var domId = resultSet.getInt("domain_object_id");
                var codId = resultSet.getInt("codomain_object_id");
                var jsonValue = resultSet.getString("json_value");
                //var schema = builder.fromJSON(jsonObject);
                output.add(new SchemaMorphismWrapper(id, domId, codId, jsonValue));
            }
        });
    }

    public SchemaMorphismWrapper find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM schema_morphism WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //var jsonValue = new JSONObject(resultSet.getString("json_value"));
                var domId = resultSet.getInt("domain_object_id");
                var codId = resultSet.getInt("codomain_object_id");
                var jsonValue = resultSet.getString("json_value");
                //var schema = new SchemaCategory.Builder().fromJSON(jsonObject);
                output.set(new SchemaMorphismWrapper(id, domId, codId, jsonValue));
            }
        });
    }

    /*
    public Integer add(SchemaMorphism morphism) {
        // TODO
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
