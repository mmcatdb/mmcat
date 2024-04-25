package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class DatabaseRepository {

    @Autowired
    private DatabaseWrapper db;

    public DatasourceWrapper find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM datasource WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                output.set(DatasourceWrapper.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DatasourceWrapper> findAll() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM datasource ORDER BY id;");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(DatasourceWrapper.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DatasourceWrapper> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                    SELECT
                        DISTINCT datasource.id as id,
                        datasource.json_value as json_value
                    FROM datasource
                    JOIN logical_model on logical_model.id = datasource.id
                    WHERE logical_model.schema_category_id = ?
                    ORDER BY datasource.id;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(DatasourceWrapper.fromJsonValue(id, jsonValue));
            }
        });
    }

    public DatasourceWrapper save(DatasourceWrapper datasource) {
        return datasource.id == null ? create(datasource) : update(datasource);
    }

    private DatasourceWrapper create(DatasourceWrapper datasource) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO datasource (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, datasource.toJsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Id id = getId(generatedKeys, "id");
                output.set(new DatasourceWrapper(id, datasource));
            }
        });
    }

    private DatasourceWrapper update(DatasourceWrapper datasource) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("UPDATE datasource SET json_value = ?::jsonb WHERE id = ?;");
            statement.setString(1, datasource.toJsonValue());
            setId(statement, 2, datasource.id);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            output.set(new DatasourceWrapper(datasource.id, datasource));
        });
    }

    public boolean delete(Id id) {
        return db.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("DELETE FROM datasource WHERE id = ?;");
            setId(statement, 1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows == 1);
        });
    }

}
