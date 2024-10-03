package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ActionRepository {

    @Autowired
    private DatabaseWrapper db;

    public List<Action> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT *
                FROM action
                WHERE category_id = ?
                ORDER BY action.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id id = getId(resultSet, "id");
                final String jsonValue = resultSet.getString("json_value");
                output.add(Action.fromJsonValue(id, categoryId, jsonValue));
            }
        });
    }

    public Action find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT *
                FROM action
                WHERE action.id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final Id categoryId = getId(resultSet, "category_id");
                final String jsonValue = resultSet.getString("json_value");
                output.set(Action.fromJsonValue(id, categoryId, jsonValue));
            }
        });
    }

    public void save(Action action) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO action (id, category_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    category_id = EXCLUDED.category_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, action.id());
            setId(statement, 2, action.categoryId);
            statement.setString(3, action.toJsonValue());
            executeChecked(statement);
        });
    }

    public void delete(Id id) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                DELETE FROM action
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            executeChecked(statement);
        });
    }

}
