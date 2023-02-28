package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.repository.utils.Utils;

import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class SchemaCategoryRepository {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectReader schemaObjectWrapperJsonReader = mapper.readerFor(SchemaObjectWrapper.class);

    @Autowired
    private DatabaseWrapper db;

    public List<SchemaCategoryInfo> findAllInfos() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("""
                SELECT
                    id,
                    json_value::json->>'label' as label,
                    json_value::json->>'version' as version
                FROM schema_category;
                """);

            while (resultSet.next()) {
                var id = getId(resultSet, "id");
                var label = resultSet.getString("label");
                var version = new Version(resultSet.getString("version"));
                output.add(new SchemaCategoryInfo(id, label, version));
            }
        });
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    id,
                    json_value::json->>'label' as label,
                    json_value::json->>'version' as version
                FROM schema_category
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                var label = resultSet.getString("label");
                var version = new Version(resultSet.getString("version"));
                output.set(new SchemaCategoryInfo(id, label, version));
            }
        });
    }

    public SchemaCategoryWrapper find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM schema_category WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                var jsonValue = resultSet.getString("json_value");
                output.set(SchemaCategoryWrapper.fromJsonValue(id, jsonValue));
            }
        });
    }

    public Id add(SchemaCategoryWrapper wrapper) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO schema_category (json_value)
                VALUES (?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, wrapper.toJsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

    public SchemaObjectWrapper findObject(Id categoryId, Key key) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT object
                FROM
                    (SELECT * FROM schema_category WHERE id = ?) as selected_category,
                    jsonb_array_elements(selected_category.json_value->'objects') object
                WHERE object->'key' @> ?;
                """);
            setId(statement, 1, categoryId);
            statement.setString(2, Utils.toJson(key));
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                var jsonObject = resultSet.getString("object");
                output.set(schemaObjectWrapperJsonReader.readValue(jsonObject));
            }
        });
    }

}
