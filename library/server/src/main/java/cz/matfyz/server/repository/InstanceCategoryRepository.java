package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.InstanceCategoryEntity;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InstanceCategoryRepository {

    @Autowired
    private DatabaseWrapper db;

    public @Nullable InstanceCategoryEntity find(Id sessionId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    instance_data,
                    category_id
                FROM session
                WHERE id = ?;
                """);
            setId(statement, 1, sessionId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var instanceData = resultSet.getString("instance_data");
                if (instanceData == null) {
                    output.set(null);
                    return;
                }

                final var schemaCategoryId = getId(resultSet, "category_id");
                output.set(InstanceCategoryEntity.fromJsonValue(schemaCategoryId, sessionId, instanceData));
            }
        });
    }

    public void save(InstanceCategoryEntity entity) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                UPDATE session
                SET instance_data = ?::jsonb
                WHERE id = ?;
                """
            );
            statement.setString(1, entity.toJsonValue());
            setId(statement, 2, entity.sessionId());
            executeChecked(statement);
        });
    }

}
