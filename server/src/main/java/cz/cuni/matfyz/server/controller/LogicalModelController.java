package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.DatabaseInfo;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelFull;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInfo;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.LogicalModelService;

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

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/logical-models/{id}")
    public LogicalModelFull getLogicalModel(@PathVariable Id id) {
        var logicalModel = service.findFull(id);

        if (logicalModel == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return logicalModel;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @GetMapping("/schema-categories/{categoryId}/logical-models")
    public List<LogicalModelFull> getAllLogicalModelsInCategory(@PathVariable Id categoryId) {
        return service.findAllFull(categoryId);
    }

    @PostMapping("/logical-models")
    public LogicalModelInfo createNewLogicalModel(@RequestBody LogicalModelInit init) {
        return service.createNew(init).toInfo();
    }

    private record LogicalModelDatabaseInfo(
        LogicalModelInfo logicalModel,
        DatabaseInfo database
    ) {}

    @GetMapping("/schema-categories/{categoryId}/logical-model-infos")
    public List<LogicalModelDatabaseInfo> getAllLogicalModelDatabaseInfosInCategory(@PathVariable Id categoryId) {
        var databases = databaseService.findAll();


        return service.findAll(categoryId).stream().map(logicalModel -> {
            var database = databases.stream().filter(d -> d.id.equals(logicalModel.databaseId)).findFirst();

            return new LogicalModelDatabaseInfo(
                logicalModel.toInfo(),
                database.isPresent() ? database.get().toInfo() : null
            );
        }).toList();
    }

}
