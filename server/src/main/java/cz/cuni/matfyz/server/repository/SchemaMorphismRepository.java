package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaMorphismUpdateFixed;
import cz.cuni.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class SchemaMorphismRepository {
    
    public List<SchemaMorphismWrapper> findAllInCategory(Id categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_morphism
                JOIN schema_morphism_in_category ON schema_morphism_id = schema_morphism.id
                WHERE schema_category_id = ?;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                //String jsonValue = new JSONObject(resultSet.getString("json_value"));
                Id domId = getId(resultSet, "domain_object_id");
                Id codId = getId(resultSet, "codomain_object_id");
                String jsonValue = resultSet.getString("json_value");
                //var schema = builder.fromJSON(jsonObject);
                output.add(new SchemaMorphismWrapper(id, domId, codId, jsonValue));
            }
        });
    }

    public SchemaMorphismWrapper find(Id id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM schema_morphism WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //String jsonValue = new JSONObject(resultSet.getString("json_value"));
                Id domId = getId(resultSet, "domain_object_id");
                Id codId = getId(resultSet, "codomain_object_id");
                String jsonValue = resultSet.getString("json_value");
                //var schema = new SchemaCategory.Builder().fromJSON(jsonObject);
                output.set(new SchemaMorphismWrapper(id, domId, codId, jsonValue));
            }
        });
    }

    public Id add(SchemaMorphismUpdateFixed morphism, Id categoryId) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO schema_morphism (domain_object_id, codomain_object_id, json_value) VALUES (?, ?, ?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            
            setId(statement, 1, morphism.domId());
            setId(statement, 2, morphism.codId());
            statement.setString(3, morphism.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next())
                return;

            var generatedId = getId(generatedKeys, "id");

            var categoryStatement = connection.prepareStatement("INSERT INTO schema_morphism_in_category (schema_category_id, schema_morphism_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            setId(categoryStatement, 1, categoryId);
            setId(categoryStatement, 2, generatedId);

            int categoryAffectedRows = categoryStatement.executeUpdate();
            if (categoryAffectedRows == 0)
                return;

            output.set(generatedId);
        });
    }

}
