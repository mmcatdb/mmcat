package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DatasourceRepository {

    @Autowired
    private DatabaseWrapper db;

    public DatasourceWrapper find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("SELECT * FROM datasource WHERE id = ?;");
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final String jsonValue = resultSet.getString("json_value");
                output.set(DatasourceWrapper.fromJsonValue(id, jsonValue));
            }
        },
        "Datasource", id);
    }

    public List<DatasourceWrapper> findAll() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("SELECT * FROM datasource ORDER BY id;");
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id id = getId(resultSet, "id");
                final String jsonValue = resultSet.getString("json_value");
                output.add(DatasourceWrapper.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DatasourceWrapper> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    DISTINCT datasource.id as id,
                    datasource.json_value as json_value
                FROM datasource
                JOIN logical_model on logical_model.datasource_id = datasource.id
                WHERE logical_model.schema_category_id = ?
                ORDER BY datasource.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id id = getId(resultSet, "id");
                final String jsonValue = resultSet.getString("json_value");
                output.add(DatasourceWrapper.fromJsonValue(id, jsonValue));
            }
        });
    }

    public boolean save(DatasourceWrapper datasource) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO datasource (id, json_value)
                VALUES (?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, datasource.id);
            statement.setString(2, datasource.toJsonValue());

            output.set(statement.executeUpdate() != 0);
        });
    }

    public boolean delete(Id id) {
        return db.getBoolean((connection, output) -> {
            final var statement = connection.prepareStatement("DELETE FROM datasource WHERE id = ?;");
            setId(statement, 1, id);

            output.set(statement.executeUpdate() != 0);
        });
    }

}

