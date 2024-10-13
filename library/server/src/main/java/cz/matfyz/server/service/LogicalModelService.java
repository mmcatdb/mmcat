package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.LogicalModel;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LogicalModelService {

    @Autowired
    private LogicalModelRepository repository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    public record LogicalModelWithMappings(
        LogicalModel logicalModel,
        DatasourceWrapper datasource,
        List<MappingWrapper> mappings
    ) {}

    public LogicalModelWithMappings findFull(Id logicalModelId) {
        final var model = repository.find(logicalModelId);
        final var mappings = mappingRepository.findAll(logicalModelId);

        return new LogicalModelWithMappings(model.logicalModel(), model.datasource(), mappings);
    }

    public List<LogicalModelWithMappings> findAllFull(Id categoryId) {
        return repository.findAllInCategory(categoryId).stream().map(model -> {
            final var mappings = mappingRepository.findAll(model.logicalModel().id());

            return new LogicalModelWithMappings(model.logicalModel(), model.datasource(), mappings);
        }).toList();
    }

    public LogicalModelWithDatasource create(
        Id categoryId,
        Id datasourceId,
        String label
    ) {
        final var datasource = datasourceRepository.find(datasourceId);
        final var logicalModel = LogicalModel.createNew(categoryId, datasourceId, label);
        repository.save(logicalModel);

        return new LogicalModelWithDatasource(logicalModel, datasource);
    }

}
