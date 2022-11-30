package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseView;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelView;
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

    // TODO - jobs should depend on the logical model instead of mapping
    // TODO - LogicalModel vs LogicalModelView - now we have to load the whole view for each mapping ...
        // TODO - also some unification for the naming - i.e., View vs Full
    // TODO - rename name to label (almost) everywhere

    @Autowired
    private LogicalModelService service;

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/logical-models/{id}")
    public LogicalModelView getLogicalModel(@PathVariable int id) {
        LogicalModel model = service.find(id);

        if (model == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return logicalModelToView(model);
    }

    @GetMapping("/schema-categories/{categoryId}/logical-models")
    public List<LogicalModelView> getAllLogicalModelsInCategory(@PathVariable int categoryId) {
        return service.findAllInCategory(categoryId).stream().map(this::logicalModelToView).toList();
    }

    @PostMapping("/logical-models")
    public LogicalModelView createNewLogicalModel(@RequestBody LogicalModelInit newLogicalModel) {
        return logicalModelToView(service.createNew(newLogicalModel));
    }

    private LogicalModelView logicalModelToView(LogicalModel model) {
        Database database = databaseService.find(model.databaseId);
        DatabaseView databaseView = new DatabaseView(database, databaseService.getDatabaseConfiguration(database));
        return new LogicalModelView(model, databaseView);
    }

}
