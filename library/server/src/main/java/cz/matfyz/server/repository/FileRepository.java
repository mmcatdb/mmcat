package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileRepository {

    @Autowired
    private DatabaseWrapper db;

    public void save(File file) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO file (id, job_id, datasource_id, label, file_type)
                VALUES (?, ?, ?, ?, ?)
                """);
            setId(statement, 1, file.id());
            setId(statement, 2, file.jobId);
            setId(statement, 3, file.datasourceId);
            statement.setString(4, file.label);
            statement.setString(5, file.fileType.toString());
            executeChecked(statement);
        });
    }
}
