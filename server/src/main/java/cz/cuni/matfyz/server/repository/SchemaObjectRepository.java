package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectUpdate;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.utils.Position;

import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class SchemaObjectRepository {

    public List<SchemaObjectWrapper> findAllInCategory(Id categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_object
                JOIN schema_object_in_category ON (schema_object_id = schema_object.id)
                WHERE schema_category_id = ?;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                String jsonObject = resultSet.getString("json_value");
                String jsonPosition = resultSet.getString("position");
                Position position = new Position.Builder().fromJSON(jsonPosition);
                //var schema = builder.fromJSON(jsonObject);

                if (position != null)
                    output.add(new SchemaObjectWrapper(getId(resultSet, "id"), jsonObject, position));
            }
        });
    }

    public SchemaObjectWrapper find(Id id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM schema_object WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                var jsonObject = resultSet.getString("json_value");
                //var schema = new SchemaObject.Builder().fromJSON(jsonObject);
                output.set(new SchemaObjectWrapper(getId(resultSet, "id"), jsonObject, null));
            }
        });
    }

    public boolean updatePosition(Id categoryId, Id objectId, Position newPosition) {
        return DatabaseWrapper.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("""
                UPDATE schema_object_in_category
                SET position = ?::jsonb
                WHERE schema_category_id = ?
                    AND schema_object_id = ?;
                """);
            statement.setString(1, newPosition.toJSON().toString());
            setId(statement, 2, categoryId);
            setId(statement, 3, objectId);

            int affectedRows = statement.executeUpdate();

            output.set(affectedRows > 0);
        });
    }

    // TODO This should be handled by one transaction.
    public Id add(SchemaObjectUpdate object, Id categoryId) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO schema_object (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, object.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next())
                return;

            var generatedId = getId(generatedKeys, "id");

            var categoryStatement = connection.prepareStatement("INSERT INTO schema_object_in_category (schema_category_id, schema_object_id, position) VALUES (?, ?, ?::jsonb)", Statement.RETURN_GENERATED_KEYS);
            setId(categoryStatement, 1, categoryId);
            setId(categoryStatement, 2, generatedId);
            categoryStatement.setString(3, object.position().toJSON().toString());

            int categoryAffectedRows = categoryStatement.executeUpdate();
            if (categoryAffectedRows == 0)
                return;

            output.set(generatedId);
        });
    }

}
