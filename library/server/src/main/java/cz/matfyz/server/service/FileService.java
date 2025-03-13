package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.entity.file.File.FileType;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.FileRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    public File create(@Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, String label, DatasourceType datasourceType, String contents) {
        final var file = File.createnew(jobId, datasourceId, categoryId, label, getFileType(datasourceType), contents, uploads);
        repository.save(file);

        return file;
    }
    private FileType getFileType(DatasourceType datasourceType) {
        return switch (datasourceType) {
            case mongodb, postgresql, neo4j -> FileType.DML;
            case csv -> FileType.CSV;
            default -> FileType.JSON;
        };
    }

    public List<File> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public void executeDML(File file) {
        final DatasourceWrapper datasourceWrapper = datasourceRepository.find(file.datasourceId);
        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasourceWrapper);
        final Path filePath = Paths.get(File.getFilePath(file, uploads));

        LOGGER.info("Start executing models ...");
        control.execute(filePath);
        LOGGER.info("... models executed.");
    }

    public File updateFileLabel(Id id, String newLabel) {
        final File file = repository.find(id);
        file.updateLabel(newLabel);
        repository.save(file);
        return file;
    }

}
