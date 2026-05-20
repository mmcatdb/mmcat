package cz.matfyz.server.file;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceInit;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.datasource.WrapperService;
import cz.matfyz.server.utils.Configuration.UploadsProperties;
import cz.matfyz.server.utils.entity.Id;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileRepository repository;

    @Autowired
    private UploadsProperties uploads;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private DatasourceRepository datasourceRepository;

    public File create(@Nullable Id jobId, @Nullable Id datasourceId, boolean isExecuted, DatasourceType datasourceType, String content) {
        final var file = File.createNew(jobId, datasourceId, isExecuted, datasourceType);

        repository.save(file);

        writeToFile(file, content);

        return file;
    }

    private void writeToFile(File file, String content) {
        final var path = file.path(uploads);

        try {
            Files.createDirectories(Paths.get(uploads.directory()));
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + path, e);
        }
    }

    public enum DMLExecutionMode {
        EXECUTE,
        CREATE_NEW_AND_EXECUTE,
        DELETE_AND_EXECUTE
    }

    public File executeDML(File file, DMLExecutionMode mode, String newDBName) {
        final DatasourceEntity datasourceEntity = datasourceRepository.find(file.datasourceId);
        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasourceEntity);
        final Path path = file.path(uploads);

        // FIXME Unify somehow. We should be able to decide based on parameters (newDBNamae, delete).
        switch (mode) {
            case DMLExecutionMode.CREATE_NEW_AND_EXECUTE:
                executeWithNewDatabase(path, control, datasourceEntity, newDBName);
                break;
            case DMLExecutionMode.DELETE_AND_EXECUTE:
                executeWithDelete(path, control, file);
                break;
            case DMLExecutionMode.EXECUTE:
                execute(path, control);
                break;
        }

        file.addExecutionDate(new Date());
        repository.save(file);

        return file;
    }

    private void executeWithNewDatabase(Path path, AbstractControlWrapper oldControl, DatasourceEntity datasourceEntity, String newDBName) {
        LOGGER.info("Creating new database and executing models...");

        final AbstractDDLWrapper oldDDLWrapper = oldControl.getDDLWrapper();
        // Here similarly as for "database" field. I am in trouble if the field names change
        final AbstractStatement creationStatement = oldDDLWrapper.createCreationStatement(newDBName, datasourceEntity.settings.get("username").asText());

        // this could be only executed if the datasource isClonable (which in most cases mean that the current user has admin privileges)
        oldControl.execute(Collections.singletonList(creationStatement));

        // This approach is ok for MongoDB, PostgreSQL and Neo4j, since their settings all have the "database" field
        // (will it be ok on other DBs?)
        final ObjectNode newSettings = datasourceEntity.settings.put("database", newDBName);
        final DatasourceInit newDataSourceInit = new DatasourceInit(datasourceEntity.label, datasourceEntity.type, newSettings);
        final DatasourceEntity newDatasourceEntity = DatasourceEntity.createNew(newDataSourceInit);
        final AbstractControlWrapper newControl = wrapperService.getControlWrapper(newDatasourceEntity);

        newControl.execute(path);
        LOGGER.info("... models executed");

        datasourceRepository.save(newDatasourceEntity);
    }

    private void executeWithDelete(Path path, AbstractControlWrapper control, File file) {
        final AbstractDDLWrapper ddlWrapper = control.getDDLWrapper();
        final Collection<AbstractStatement> deleteStatements = ddlWrapper.createDDLDeleteStatements(readExecutionCommands(file));

        LOGGER.info("Start executing delete statements ...");
        control.execute(deleteStatements);
        LOGGER.info("... delete statements executed.");

        execute(path, control);
    }

    private void execute(Path path, AbstractControlWrapper control) {
        LOGGER.info("Start executing models ...");
        control.execute(path);
        LOGGER.info("... models executed.");
    }

    private List<String> readExecutionCommands(File file) {
        final var path = file.path(uploads);

        try {
            String fileContents = Files.readString(path);
            fileContents = fileContents.replaceAll("\\s+", " ").trim();
            // FIXME This is SUS af.
            return List.of(fileContents.split("(?<=;)(?=(?:[^\"']*[\"'][^\"']*[\"'])*[^\"']*$)"));

        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read execution file: " + path, e);
        }
    }

    public String readPreview(Id id, int lineLimit) {
        final File file = repository.find(id);
        final var path = file.path(uploads);

        final var sb = new StringBuilder();
        int linesRead = 0;

        try (
            var reader = Files.newBufferedReader(path)
        ) {
            String line;
            while ((line = reader.readLine()) != null && linesRead < lineLimit) {
                sb.append(line).append("\n");
                linesRead++;
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read preview for file: " + path, e);
        }

        return sb.toString();
    }

    public record FileEdit(String label, String description) {}

    public File updateFile(Id id, FileEdit edit) {
        final File file = repository.find(id);

        file.label = edit.label;
        file.description = edit.description;

        repository.save(file);
        return file;
    }

}
