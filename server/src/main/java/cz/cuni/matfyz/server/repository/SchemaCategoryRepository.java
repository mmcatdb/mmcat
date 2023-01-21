package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class SchemaCategoryRepository {

    @Autowired
    private DatabaseWrapper db;

    public List<SchemaCategoryInfo> findAll() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT * FROM schema_category;");

            while (resultSet.next()) {
                var id = getId(resultSet, "id");
                //var jsonValue = new JSONObject(resultSet.getString("json_value"));
                var jsonValue = resultSet.getString("json_value");
                output.add(new SchemaCategoryInfo(id, jsonValue));
            }
        });
    }

    public SchemaCategoryInfo find(Id id) {
        return db.get((connection, output) -> {

            var statement = connection.prepareStatement("SELECT * FROM schema_category WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                var jsonValue = resultSet.getString("json_value");
                output.set(new SchemaCategoryInfo(id, jsonValue));
            }
        });
    }

    public Id add(SchemaCategoryInit init) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO schema_category (version, json_value)
                VALUES (?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, "0"); // TODO version
            statement.setString(2, init.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
