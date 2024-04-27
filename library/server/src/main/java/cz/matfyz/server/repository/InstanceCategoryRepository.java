package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.instance.InstanceCategoryWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InstanceCategoryRepository {

    @Autowired
    private DatabaseWrapper db;

    public @Nullable InstanceCategoryWrapper find(Id sessionId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    instance_data,
                    schema_category_id
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

                final var schemaCategoryId = getId(resultSet, "schema_category_id");
                output.set(InstanceCategoryWrapper.fromJsonValue(schemaCategoryId, sessionId, instanceData));
            }
        });
    }

    public boolean save(InstanceCategoryWrapper wrapper) {
        return db.getBoolean((connection, output) -> {
            final var statement = connection.prepareStatement("""
                UPDATE session
                SET instance_data = ?::jsonb
                WHERE id = ?;
                """
            );
            statement.setString(1, wrapper.toJsonValue());
            setId(statement, 2, wrapper.sessionId());

            final int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

}
