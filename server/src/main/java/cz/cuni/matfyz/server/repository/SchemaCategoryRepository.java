package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
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

    public Integer add(SchemaCategoryInit init) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO schema_category (json_value)
                VALUES (?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, init.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(generatedKeys.getInt("id"));
        });
    }

}
