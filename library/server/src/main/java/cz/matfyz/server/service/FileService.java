package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.FileRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.server.global.Configuration.UploadsProperties;

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

    public File create(@Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, String jobLabel, boolean executed, DatasourceType datasourceType, String contents) {
        final var file = File.createnew(jobId, datasourceId, categoryId, jobLabel, executed, datasourceType, contents, uploads);
        repository.save(file);

        return file;
    }

    public List<File> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public File executeDML(File file, String mode, String newDBName) {
        final DatasourceEntity datasourceEntity = datasourceRepository.find(file.datasourceId);
        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasourceEntity);
        final Path filePath = Paths.get(File.getFilePath(file, uploads));

        switch (mode) {
            case "create_new_and_execute":
                executeWithNewDatabase(filePath, control, datasourceEntity, newDBName);
                break;
            case "delete_and_execute":
                executeWithDelete(filePath, control, file);
                break;
            default:
                execute(filePath, control);
                break;
        }

        updateExecutionDate(file);
        return file;
    }

    private void executeWithNewDatabase(Path filePath, AbstractControlWrapper oldControl, DatasourceEntity datasourceEntity, String newDBName) {
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

        newControl.execute(filePath);
        LOGGER.info("... models executed");

        datasourceRepository.save(newDatasourceEntity);
    }

    private void executeWithDelete(Path filePath, AbstractControlWrapper control, File file) {
        final AbstractDDLWrapper ddlWrapper = control.getDDLWrapper();
        final Collection<AbstractStatement> deleteStatements = ddlWrapper.createDDLDeleteStatements(file.readExecutionCommands(uploads));

        LOGGER.info("Start executing delete statements ...");
        control.execute(deleteStatements);
        LOGGER.info("... delete statements executed.");

        execute(filePath, control);
    }

    private void execute(Path filePath, AbstractControlWrapper control) {
        LOGGER.info("Start executing models ...");
        control.execute(filePath);
        LOGGER.info("... models executed.");
    }

    private void updateExecutionDate(File file) {
        file.addExecutionDate(new Date());
        repository.save(file);
    }

    /** Like PATCH (if null, the value shouldn't be edited). */
    public record FileEdit(@Nullable String label, @Nullable String description) {}

    public File updateFile(Id id, FileEdit edit) {
        final File file = repository.find(id);

        if (edit.label != null)
            file.label = edit.label;
        if (edit.description != null)
            file.description = edit.description;

        repository.save(file);
        return file;
    }

    public String readPreview(Id id) {
        final File file = repository.find(id);
        return file.readPreview(uploads);
    }

}
