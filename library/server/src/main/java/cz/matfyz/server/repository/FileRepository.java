package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileRepository {

    @Autowired
    private DatabaseWrapper db;

    public void save(File file) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO "file" (id, job_id, datasource_id, category_id, json_value)
                VALUES (?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, file.id());
            setId(statement, 2, file.jobId);
            setId(statement, 3, file.datasourceId);
            setId(statement, 4, file.categoryId);
            statement.setString(5, file.toJsonValue());
            executeChecked(statement);
        });
    }

    public List<File> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT *
                FROM "file"
                WHERE category_id = ?
                ORDER BY file.id;
            """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id id = getId(resultSet, "id");
                final Id jobId = getId(resultSet, "job_id");
                final Id datasourceId = getId(resultSet, "datasource_id");
                final String jsonValue = resultSet.getString("json_value");
                output.add(File.fromJsonValue(id, jobId, datasourceId, categoryId, jsonValue));
            }
        });
    }

    public File find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT *
                FROM "file"
                WHERE file.id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final Id jobId = getId(resultSet, "job_id");
                final Id datasourceId = getId(resultSet, "datasource_id");
                final Id categoryId = getId(resultSet, "category_id");
                final String jsonValue = resultSet.getString("json_value");
                output.set(File.fromJsonValue(id, jobId, datasourceId, categoryId, jsonValue));
            }
        });
    }

}
