package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.view.SchemaCategoryUpdate;

import java.sql.Statement;
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

    public Integer add(SchemaCategoryUpdate.MorphismUpdate morphism, int categoryId) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO schema_morphism (domain_object_id, codomain_object_id, json_value) VALUES (?, ?, ?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            
            statement.setInt(1, morphism.domId);
            statement.setInt(2, morphism.codId);
            statement.setString(3, morphism.jsonValue);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next())
                return;

            var generatedId = generatedKeys.getInt("id");

            var categoryStatement = connection.prepareStatement("INSERT INTO schema_morphism_in_category (schema_category_id, schema_morphism_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            categoryStatement.setInt(1, categoryId);
            categoryStatement.setInt(2, generatedId);

            int categoryAffectedRows = categoryStatement.executeUpdate();
            if (categoryAffectedRows == 0)
                return;

            output.set(generatedId);
        });
    }

}
