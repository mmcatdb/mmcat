package cz.matfyz.server.controller;

import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceInfo;
import cz.matfyz.server.entity.datasource.DatasourceWithConfiguration;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;
import cz.matfyz.server.service.LogicalModelService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class LogicalModelController {

    // TODO - LogicalModel vs LogicalModelView - now we have to load the whole view for each mapping ...
    // TODO - also some unification for the naming - i.e., View vs Full

    @Autowired
    private LogicalModelService service;

    public record LogicalModelDetail(
        Id id,
        Id categoryId,
        DatasourceWithConfiguration datasource,
        String label,
        List<MappingWrapper> mappings
    ) implements IEntity {
        public static LogicalModelDetail fromEntities(LogicalModel model, DatasourceWithConfiguration datasource, List<MappingWrapper> mappings) {
            return new LogicalModelDetail(
                model.id,
                model.categoryId,
                datasource,
                model.label,
                mappings
            );
        }
    }

    @GetMapping("/logical-models/{id}")
    public LogicalModelDetail getLogicalModel(@PathVariable Id id) {
        var logicalModel = service.findFull(id);

        if (logicalModel == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return logicalModel;
    }

    @Deprecated
    @GetMapping("/schema-categories/{categoryId}/logical-models")
    public List<LogicalModelDetail> getAllLogicalModelsInCategory(@PathVariable Id categoryId) {
        return service.findAllFull(categoryId);
    }

    public record LogicalModelInfo(
        Id id,
        String label,
        DatasourceInfo datasource
    ) {
        public static LogicalModelInfo fromEntities(LogicalModelWithDatasource modelWithDatasource) {
            return new LogicalModelInfo(
                modelWithDatasource.logicalModel().id,
                modelWithDatasource.logicalModel().label,
                modelWithDatasource.datasource().toInfo()
            );
        }
    }

    @PostMapping("/logical-models")
    public LogicalModelInfo createNewLogicalModel(@RequestBody LogicalModelInit init) {
        return LogicalModelInfo.fromEntities(service.createNew(init));
    }

    @GetMapping("/schema-categories/{categoryId}/logical-model-infos")
    public List<LogicalModelInfo> getAllLogicalModelInfosInCategory(@PathVariable Id categoryId) {
        return service.findAll(categoryId).stream().map(LogicalModelInfo::fromEntities).toList();
    }

}
