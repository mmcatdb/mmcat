package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.Evolution;
import cz.matfyz.server.entity.evolution.MappingEvolution;
import cz.matfyz.server.entity.evolution.QueryEvolution;
import cz.matfyz.server.entity.evolution.SchemaEvolution;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EvolutionRepository {

    @Autowired
    private DatabaseWrapper db;

    private void createBase(Evolution evolution) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO evolution (id, category_id, version, type)
                VALUES (?, ?, ?, ?);
                """);
            setId(statement, 1, evolution.id());
            setId(statement, 2, evolution.categoryId);
            statement.setString(3, evolution.version.toString());
            statement.setString(4, evolution.type.toString());
            executeChecked(statement);
        });
    }

    public void create(SchemaEvolution evolution) {
        createBase(evolution);

        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO category_evolution (id, json_value)
                VALUES (?, ?::jsonb);
                """);
            setId(statement, 1, evolution.id());
            statement.setString(2, evolution.toJsonValue());
            executeChecked(statement);
        });
    }

    public void create(MappingEvolution evolution) {
        createBase(evolution);

        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO mapping_evolution (id, mapping_id, json_value)
                VALUES (?, ?, ?::jsonb);
                """);
            setId(statement, 1, evolution.id());
            setId(statement, 2, evolution.mappingId);
            statement.setString(3, evolution.toJsonValue());
            executeChecked(statement);
        });
    }

    public void create(QueryEvolution evolution) {
        createBase(evolution);

        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO query_evolution (id, query_id, json_value)
                VALUES (?, ?, ?::jsonb);
                """);
            setId(statement, 1, evolution.id());
            setId(statement, 2, evolution.queryId);
            statement.setString(3, evolution.toJsonValue());
            executeChecked(statement);
        });
    }

    public List<SchemaEvolution> findAllSchemaEvolutions(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    evolution.id as id,
                    evolution.version as version,
                    category_evolution.json_value as json_value
                FROM category_evolution
                JOIN evolution ON category_evolution.id = evolution.id
                WHERE evolution.category_id = ?
                ORDER BY evolution.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final var id = getId(resultSet, "id");
                final var version = Version.fromString(resultSet.getString("version"));
                final var jsonValue = resultSet.getString("json_value");
                output.add(SchemaEvolution.fromJsonValue(id, categoryId, version, jsonValue));
            }
        });
    }

    public List<QueryEvolution> findAllQueryEvolutions(Id queryId) {
        return findAllQueryEvolutionsInner(queryId, true);
    }

    public List<QueryEvolution> findAllQueryEvolutionsInCategory(Id categoryId) {
        return findAllQueryEvolutionsInner(categoryId, false);
    }

    private List<QueryEvolution> findAllQueryEvolutionsInner(Id groupId, boolean isByQuery) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    evolution.id as id,
                    evolution.category_id as category_id,
                    evolution.version as version,
                    query_evolution.query_id as query_id,
                    query_evolution.json_value as json_value
                FROM query_evolution
                JOIN evolution ON query_evolution.id = evolution.id
                """ + (isByQuery ? "WHERE query_evolution.query_id = ?" : "WHERE evolution.category_id = ?") + """
                ORDER BY evolution.id;
                """);
            setId(statement, 1, groupId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final var id = getId(resultSet, "id");
                final var categoryId = getId(resultSet, "category_id");
                final var version = Version.fromString(resultSet.getString("version"));
                final var queryId = getId(resultSet, "query_id");
                final var jsonValue = resultSet.getString("json_value");
                output.add(QueryEvolution.fromJsonValue(id, categoryId, version, queryId, jsonValue));
            }
        });
    }

    public QueryEvolution findQueryEvolution(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    evolution.category_id as category_id,
                    evolution.version as version,
                    query_evolution.query_id as query_id,
                    query_evolution.json_value as json_value
                FROM query_evolution
                JOIN evolution ON query_evolution.id = evolution.id
                WHERE query_evolution.id = ?
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var categoryId = getId(resultSet, "category_id");
                final var version = Version.fromString(resultSet.getString("version"));
                final var queryId = getId(resultSet, "query_id");
                final var jsonValue = resultSet.getString("json_value");
                output.set(QueryEvolution.fromJsonValue(id, categoryId, version, queryId, jsonValue));
            }
        });
    }

    public List<MappingEvolution> findAllMappingEvolutions(Id mappingId) {
        return findAllMappingEvolutionsInner(mappingId, true);
    }

    public List<MappingEvolution> findAllMappingEvolutionsInCategory(Id categoryId) {
        return findAllMappingEvolutionsInner(categoryId, false);
    }

    private List<MappingEvolution> findAllMappingEvolutionsInner(Id groupId, boolean isByMapping) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    evolution.id as id,
                    evolution.category_id as category_id,
                    evolution.version as version,
                    mapping_evolution.mapping_id as mapping_id,
                    mapping_evolution.json_value as json_value
                FROM mapping_evolution
                JOIN evolution ON mapping_evolution.id = evolution.id
                """ + (isByMapping ? "WHERE mapping_evolution.mapping_id = ?" : "WHERE evolution.category_id = ?") + """
                ORDER BY evolution.id;
                """);
            setId(statement, 1, groupId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final var id = getId(resultSet, "id");
                final var categoryId = getId(resultSet, "category_id");
                final var version = Version.fromString(resultSet.getString("version"));
                final var mappingId = getId(resultSet, "mapping_id");
                final var jsonValue = resultSet.getString("json_value");
                output.add(MappingEvolution.fromJsonValue(id, categoryId, version, mappingId, jsonValue));
            }
        });
    }

    public void deleteQueryEvolutions(Id queryId) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                DELETE FROM evolution
                USING query_evolution
                WHERE evolution.id = query_evolution.id
                    AND query_evolution.query_id = ?;
                """);
            setId(statement, 1, queryId);
            executeChecked(statement);
        });
    }

}
