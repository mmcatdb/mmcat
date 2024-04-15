package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datainput.DataInputEntity;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class DataInputRepository {

    @Autowired
    private DatabaseWrapper db;

    public DataInputEntity find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM data_input WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                output.set(DataInputEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DataInputEntity> findAll() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM data_input ORDER BY id;");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(DataInputEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DataInputEntity> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                    SELECT
                        DISTINCT data_input.id as id,
                        data_input.json_value as json_value
                    FROM data_input
                    JOIN logical_model on logical_model.id = data_input.id
                    WHERE logical_model.schema_category_id = ?
                    ORDER BY data_input.id;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(DataInputEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public DataInputEntity save(DataInputEntity dataInput) {
        return dataInput.id == null ? create(dataInput) : update(dataInput);
    }

    private DataInputEntity create(DataInputEntity dataInput) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO data_input (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, dataInput.toJsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Id id = getId(generatedKeys, "id");
                output.set(new DataInputEntity(id, dataInput));
            }
        });
    }

    private DataInputEntity update(DataInputEntity dataInput) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("UPDATE data_input SET json_value = ?::jsonb WHERE id = ?;");
            statement.setString(1, dataInput.toJsonValue());
            setId(statement, 2, dataInput.id);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            output.set(new DataInputEntity(dataInput.id, dataInput));
        });
    }

    public boolean delete(Id id) {
        return db.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("DELETE FROM data_input WHERE id = ?;");
            setId(statement, 1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows == 1);
        });
    }

}

