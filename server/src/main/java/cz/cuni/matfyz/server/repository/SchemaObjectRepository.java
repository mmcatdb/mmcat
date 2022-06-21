package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.schema.SchemaObjectUpdate;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.utils.Position;

import java.sql.Statement;
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

    // TODO This should be handeld by one transaction.
    public Integer add(SchemaObjectUpdate object, int categoryId) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO schema_object (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, object.jsonValue);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next())
                return;

            var generatedId = generatedKeys.getInt("id");

            var categoryStatement = connection.prepareStatement("INSERT INTO schema_object_in_category (schema_category_id, schema_object_id, position) VALUES (?, ?, ?::jsonb)", Statement.RETURN_GENERATED_KEYS);
            categoryStatement.setInt(1, categoryId);
            categoryStatement.setInt(2, generatedId);
            categoryStatement.setString(3, object.position.toJSON().toString());

            int categoryAffectedRows = categoryStatement.executeUpdate();
            if (categoryAffectedRows == 0)
                return;

            output.set(generatedId);
        });
    }

}
