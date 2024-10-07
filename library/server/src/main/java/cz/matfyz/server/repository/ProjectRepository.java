package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.DatabaseWrapper;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepository {

    @Autowired
    private DatabaseWrapper db;

    public Version getVersionByCategory(Id categoryId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT json_value::json->>'systemVersion' as systemVersion
                FROM schema_category
                WHERE id = ?;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(Version.fromString(resultSet.getString("systemVersion")));
        });
    }

    public Version getVersionByLogicalModel(Id modelId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT schema_category.json_value::json->>'systemVersion' as systemVersion
                FROM schema_category
                JOIN logical_model ON schema_category.id = logical_model.category_id
                WHERE logical_model.id = ?;
                """);
            setId(statement, 1, modelId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(Version.fromString(resultSet.getString("systemVersion")));
        });
    }

}
