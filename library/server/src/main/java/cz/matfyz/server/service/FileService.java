package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.FileRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
        final DatasourceWrapper datasourceWrapper = datasourceRepository.find(file.datasourceId);
        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasourceWrapper);
        final Path filePath = Paths.get(File.getFilePath(file, uploads));

        if (mode.equals("create_new_and_execute")) {
            // This approach is ok for MongoDB, PostgreSQL and Neo4j, since their settings all have the "database" field (will it be ok on other dbs?)
            // The "database" field gets overwritten
            ObjectNode newSettings = datasourceWrapper.settings.put("database", newDBName);
            DatasourceInit newDataSourceInit = new DatasourceInit(datasourceWrapper.label, datasourceWrapper.type, newSettings);
            final DatasourceWrapper newDatasourceWrapper = DatasourceWrapper.createNew(newDataSourceInit);
            //datasourceRepository.save(newDatasourceWrapper); // Do I want to save it?
            final AbstractControlWrapper newControl = wrapperService.getControlWrapper(newDatasourceWrapper);
            LOGGER.info("Start executing models ...");
            newControl.execute(filePath);
            LOGGER.info("... models executed.");
        } else {
            if (mode.equals("delete_and_execute")) {
                final AbstractDDLWrapper ddlWrapper = control.getDDLWrapper();
                final Collection<AbstractStatement> deleteStatements = ddlWrapper.createDDLDeleteStatements(file.readExecutionCommands(uploads));

                LOGGER.info("Start executing delete statements ...");
                control.execute(deleteStatements);
                LOGGER.info("... delete statements executed.");
            }
            LOGGER.info("Start executing models ...");
            control.execute(filePath);
            LOGGER.info("... models executed.");
        }

        file.addExecutionDate(new Date());
        repository.save(file);
        return file;
    }

    public File updateFile(Id id, String newValue, boolean isLabel) {
        final File file = repository.find(id);
        file.updateFile(newValue, isLabel);
        repository.save(file);
        return file;
    }

}
