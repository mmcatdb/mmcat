package cz.matfyz.server.file;

import cz.matfyz.server.utils.DatabaseWrapper;
import cz.matfyz.server.utils.entity.Id;

import java.util.List;

import static cz.matfyz.server.utils.Utils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileRepository {

    @Autowired
    private DatabaseWrapper db;

    public void save(File file) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO file (id, job_id, datasource_id, json_value)
                VALUES (?, ?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, file.id());
            setId(statement, 2, file.jobId);
            setId(statement, 3, file.datasourceId);
            statement.setString(4, file.toJsonValue());
            executeChecked(statement);
        });
    }

    public List<File> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    file.id as id,
                    file.job_id as job_id,
                    file.datasource_id as datasource_id,
                    file.json_value as json_value
                FROM file
                JOIN job ON job.id = file.job_id
                JOIN run ON run.id = job.run_id
                WHERE run.category_id = ?
                ORDER BY file.id;
            """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id id = getId(resultSet, "id");
                final Id jobId = getId(resultSet, "job_id");
                final Id datasourceId = getId(resultSet, "datasource_id");
                final String jsonValue = resultSet.getString("json_value");
                output.add(File.fromJsonValue(id, jobId, datasourceId, jsonValue));
            }
        });
    }

    public File find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT *
                FROM file
                WHERE file.id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final Id jobId = getId(resultSet, "job_id");
                final Id datasourceId = getId(resultSet, "datasource_id");
                final String jsonValue = resultSet.getString("json_value");
                output.set(File.fromJsonValue(id, jobId, datasourceId, jsonValue));
            }
        });
    }

}
