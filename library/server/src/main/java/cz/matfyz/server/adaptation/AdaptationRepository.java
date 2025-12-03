package cz.matfyz.server.adaptation;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.adaptation.Adaptation.AdaptationSettings;
import cz.matfyz.server.utils.DatabaseWrapper;
import cz.matfyz.server.utils.entity.Id;

import java.sql.ResultSet;
import java.sql.SQLException;

import static cz.matfyz.server.utils.Utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AdaptationRepository {

    @Autowired
    private DatabaseWrapper db;

    private static Adaptation adaptationFromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        return new Adaptation(
            getId(resultSet, "id"),
            getId(resultSet, "category_id"),
            Version.fromString(resultSet.getString("system_version")),
            getJson(resultSet, "settings", AdaptationSettings.class),
            getId(resultSet, "run_id")
        );
    }

    public Adaptation find(Id adaptationId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    adaptation.id as id,
                    adaptation.category_id as category_id,
                    adaptation.system_version as system_version,
                    adaptation.settings as settings,
                    adaptation.run_id as run_id
                FROM adaptation
                WHERE adaptation.id = ?;
                """);
            setId(statement, 1, adaptationId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(adaptationFromResultSet(resultSet));
        });
    }

    public @Nullable Adaptation tryFindByCategory(Id categoryId) {
        return db.getNullable((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    adaptation.id as id,
                    adaptation.category_id as category_id,
                    adaptation.system_version as system_version,
                    adaptation.settings as settings,
                    adaptation.run_id as run_id
                FROM adaptation
                WHERE adaptation.category_id = ?;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(adaptationFromResultSet(resultSet));
        });
    }

    public void save(Adaptation adaptation) {
        db.run(connection -> {
            // Whenever a adaptation is changed, the run is marked active again.
            final var statement = connection.prepareStatement("""
                INSERT INTO adaptation (id, category_id, system_version, settings, run_id)
                VALUES (?, ?, ?, ?::jsonb, ?)
                ON CONFLICT (id) DO UPDATE SET
                    category_id = EXCLUDED.category_id,
                    system_version = EXCLUDED.system_version,
                    settings = EXCLUDED.settings,
                    run_id = EXCLUDED.run_id;
                """);
            setId(statement, 1, adaptation.id());
            setId(statement, 2, adaptation.categoryId);
            statement.setString(3, adaptation.systemVersion.toString());
            setJson(statement, 4, adaptation.settings);
            setId(statement, 5, adaptation.runId);
            executeChecked(statement);
        });
    }

}
