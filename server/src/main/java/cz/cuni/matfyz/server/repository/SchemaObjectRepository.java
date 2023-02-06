package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectUpdate;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.utils.Position;
import cz.cuni.matfyz.server.repository.utils.Utils;

import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class SchemaObjectRepository {

    /*
    private static final ObjectReader positionJsonReader = new ObjectMapper().readerFor(Position.class);

    @Autowired
    private DatabaseWrapper db;
    
    public List<SchemaObjectWrapper> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_object
                JOIN schema_object_in_category ON schema_object_id = schema_object.id
                WHERE schema_category_id = ?;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String jsonObject = resultSet.getString("json_value");
                String jsonPosition = resultSet.getString("position");
                Position position = positionJsonReader.readValue(jsonPosition);

                if (position != null)
                    output.add(new SchemaObjectWrapper(getId(resultSet, "id"), jsonObject, position));
            }
        });
    }

    public List<SchemaObjectWrapper> findAllInLogicalModel(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    schema_object.id as id,
                    schema_object.json_value as json_value,
                    schema_object_in_category.position as position
                FROM schema_object
                JOIN mapping on mapping.root_object_id = schema_object.id
                JOIN schema_object_in_category ON schema_object_id = schema_object.id
                WHERE mapping.logical_model_id = ?;
                """);
            setId(statement, 1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String jsonObject = resultSet.getString("json_value");
                String jsonPosition = resultSet.getString("position");
                Position position = positionJsonReader.readValue(jsonPosition);

                if (position != null)
                    output.add(new SchemaObjectWrapper(getId(resultSet, "id"), jsonObject, position));
            }
        });
    }

    public boolean updatePosition(Id categoryId, Id objectId, Position newPosition) {
        return db.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("""
                UPDATE schema_object_in_category
                SET position = ?::jsonb
                WHERE schema_category_id = ?
                    AND schema_object_id = ?;
                """);
            statement.setString(1, Utils.toJson(newPosition));
            setId(statement, 2, categoryId);
            setId(statement, 3, objectId);

            int affectedRows = statement.executeUpdate();

            output.set(affectedRows > 0);
        });
    }

    // TODO This should be handled by one transaction.
    public Id add(SchemaObjectUpdate object, Id categoryId) {
        return db.get((connection, output) -> {
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
            categoryStatement.setString(3, Utils.toJson(object.position()));

            int categoryAffectedRows = categoryStatement.executeUpdate();
            if (categoryAffectedRows == 0)
                return;

            output.set(generatedId);
        });
    }
    */

}
