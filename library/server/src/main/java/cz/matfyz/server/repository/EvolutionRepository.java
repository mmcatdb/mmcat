package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EvolutionRepository {

    @Autowired
    private DatabaseWrapper db;

    public void save(SchemaUpdate update) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO evolution_update (id, category_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, update.id());
            setId(statement, 2, update.categoryId);
            statement.setString(3, update.toJsonValue());
            executeChecked(statement);
        });
    }

    public List<SchemaUpdate> findAllUpdates(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    json_value
                FROM evolution_update
                WHERE category_id = ?
                ORDER BY id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final var id = getId(resultSet, "id");
                final var jsonValue = resultSet.getString("json_value");
                output.add(SchemaUpdate.fromJsonValue(id, categoryId, jsonValue));
            }
        });
    }


}
