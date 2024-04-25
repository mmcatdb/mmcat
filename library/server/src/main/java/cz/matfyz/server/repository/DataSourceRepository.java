package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSourceEntity;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class DataSourceRepository {

    @Autowired
    private DatabaseWrapper db;

    public DataSourceEntity find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM data_source WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                output.set(DataSourceEntity.fromJsonValue(id, jsonValue));
            }
        },
        "Data source with id: {} not found.", id);
    }

    public List<DataSourceEntity> findAll() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM data_source ORDER BY id;");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(DataSourceEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DataSourceEntity> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                    SELECT
                        DISTINCT data_source.id as id,
                        data_source.json_value as json_value
                    FROM data_source
                    JOIN logical_model on logical_model.id = data_source.id
                    WHERE logical_model.schema_category_id = ?
                    ORDER BY data_source.id;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(DataSourceEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public DataSourceEntity save(DataSourceEntity dataSource) {
        return dataSource.id == null ? create(dataSource) : update(dataSource);
    }

    private DataSourceEntity create(DataSourceEntity dataSource) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO data_source (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, dataSource.toJsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Id id = getId(generatedKeys, "id");
                output.set(new DataSourceEntity(id, dataSource));
            }
        });
    }

    private DataSourceEntity update(DataSourceEntity dataSource) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("UPDATE data_source SET json_value = ?::jsonb WHERE id = ?;");
            statement.setString(1, dataSource.toJsonValue());
            setId(statement, 2, dataSource.id);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            output.set(new DataSourceEntity(dataSource.id, dataSource));
        });
    }

    public boolean delete(Id id) {
        return db.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("DELETE FROM data_source WHERE id = ?;");
            setId(statement, 1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows == 1);
        });
    }

}

