package cz.matfyz.server.service;

import cz.matfyz.server.controller.DatasourceController;
import cz.matfyz.server.controller.LogicalModelController.LogicalModelDetail;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LogicalModelService {

    @Autowired
    private LogicalModelRepository repository;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private DatasourceService datasourceService;

    public List<LogicalModelWithDatasource> findAll(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public LogicalModelWithDatasource find(Id logicalModelId) {
        return repository.find(logicalModelId);
    }

    public record LogicalModelWithMappings(
        LogicalModel logicalModel,
        DatasourceWrapper datasource,
        List<MappingWrapper> mappings
    ) {}

    public LogicalModelWithMappings findFull(Id logicalModelId) {
        final var model = find(logicalModelId);
        final var mappings = mappingService.findAll(logicalModelId);

        return new LogicalModelWithMappings(model.logicalModel(), model.datasource(), mappings);
    }

    public List<LogicalModelWithMappings> findAllFull(Id categoryId) {
        return repository.findAllInCategory(categoryId).stream().map(model -> {
            final var mappings = mappingService.findAll(model.logicalModel().id);

            return new LogicalModelWithMappings(model.logicalModel(), model.datasource(), mappings);
        }).toList();
    }

    public LogicalModelWithDatasource createNew(LogicalModelInit init) {
        final var datasource = datasourceService.find(init.datasourceId());
        final Id generatedId = repository.add(init);
        final var logicalModel = new LogicalModel(
            generatedId,
            init.categoryId(),
            init.datasourceId(),
            init.label()
        );

        return new LogicalModelWithDatasource(logicalModel, datasource);
    }
}
