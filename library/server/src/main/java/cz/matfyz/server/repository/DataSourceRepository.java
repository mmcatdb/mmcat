package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSource;
import cz.matfyz.server.repository.utils.DatabaseWrapper;
import cz.matfyz.server.repository.utils.Utils;

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

    public DataSource find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM data_source WHERE data_source.id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                output.set(new DataSource.Builder().fromJsonValue(id, jsonValue));
            }
        },
        "Data source with id: {} not found.", id);
    }

    public List<DataSource> findAll() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT *
                FROM data_source
                ORDER BY data_source.id;
                """);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(new DataSource.Builder().fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DataSource> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    DISTINCT data_source.id as id,
                    data_source.json_value as json_value
                FROM data_source
                JOIN job on job.json_value->'payload'->>'dataSourceId' = data_source.id::text
                JOIN run on job.run_id = run.id
                WHERE run.schema_category_id = ?
                ORDER BY data_source.id;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(new DataSource.Builder().fromJsonValue(id, jsonValue));
            }
        });
    }

    public DataSource save(DataSource dataSource) {
        return dataSource.id == null ? create(dataSource) : update(dataSource);
    }

    private DataSource create(DataSource dataSource) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO data_source (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, Utils.toJson(dataSource));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Id id = getId(generatedKeys, "id");
                output.set(new DataSource.Builder().fromDataSource(id, dataSource));
            }
        });
    }

    private DataSource update(DataSource dataSource) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("UPDATE data_source SET json_value = ?::jsonb WHERE id = ?;");
            statement.setString(1, Utils.toJson(dataSource));
            setId(statement, 2, dataSource.id);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            output.set(new DataSource.Builder().fromDataSource(dataSource.id, dataSource));
        });
    }

    public boolean delete(Id id) {
        return db.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("""
                DELETE FROM data_source
                WHERE id = ?;
                """);
            setId(statement, 1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

}
