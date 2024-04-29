package cz.matfyz.server.controller;

import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceDetail;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;
import cz.matfyz.server.service.LogicalModelService;
import cz.matfyz.server.service.LogicalModelService.LogicalModelWithMappings;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LogicalModelController {

    @Autowired
    private LogicalModelService service;

    @Autowired
    private DatasourceController datasourceController;

    public record LogicalModelDetail(
        Id id,
        String label,
        DatasourceDetail datasource,
        List<MappingWrapper> mappings
    ) implements IEntity {}

    private LogicalModelDetail createDetail(LogicalModelWithMappings model) {
        return new LogicalModelDetail(
            model.logicalModel().id,
            model.logicalModel().label,
            datasourceController.datasourceToDetail(model.datasource()),
            model.mappings()
        );
    }

    @GetMapping("/logical-models/{id}")
    public LogicalModelDetail getLogicalModel(@PathVariable Id id) {
        final var logicalModel = service.findFull(id);

        if (logicalModel == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return createDetail(logicalModel);
    }

    @Deprecated
    @GetMapping("/schema-categories/{categoryId}/logical-models")
    public List<LogicalModelDetail> getAllLogicalModelsInCategory(@PathVariable Id categoryId) {
        return service.findAllFull(categoryId).stream().map(this::createDetail).toList();
    }

    public record LogicalModelInfo(
        Id id,
        String label,
        DatasourceDetail datasource
    ) implements IEntity {}

    LogicalModelInfo createInfo(LogicalModelWithDatasource model) {
        return new LogicalModelInfo(
            model.logicalModel().id,
            model.logicalModel().label,
            datasourceController.datasourceToDetail(model.datasource())
        );
    }

    @PostMapping("/logical-models")
    public LogicalModelInfo createNewLogicalModel(@RequestBody LogicalModelInit init) {
        return createInfo(service.createNew(init));
    }

    @GetMapping("/schema-categories/{categoryId}/logical-model-infos")
    public List<LogicalModelInfo> getAllLogicalModelInfosInCategory(@PathVariable Id categoryId) {
        return service.findAll(categoryId).stream().map(this::createInfo).toList();
    }

}
