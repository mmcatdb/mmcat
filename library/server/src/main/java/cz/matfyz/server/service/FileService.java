package cz.matfyz.server.service;

import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.entity.file.File.FileType;
import cz.matfyz.server.repository.FileRepository;
import cz.matfyz.server.global.Configuration.UploadsProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.checkerframework.checker.nullness.qual.Nullable;

@Service
public class FileService {

    @Autowired
    private FileRepository repository;

    @Autowired
    private UploadsProperties uploads;

    public File create(@Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, DatasourceType datasourceType, String contents) {
        final var file = File.createnew(jobId, datasourceId, categoryId, getFileType(datasourceType), contents, uploads);
        repository.save(file);

        return file;
    }

    private FileType getFileType(DatasourceType datasourceType) {
        FileType fileType = switch (datasourceType) {
            case mongodb, postgresql, neo4j -> FileType.DML;
            default -> FileType.DATA_FILE;
        };

        return fileType;
    }
}
