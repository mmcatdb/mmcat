package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseView;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelView;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingView;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.LogicalModelService;
import cz.cuni.matfyz.server.service.MappingService;

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
public class MappingController {

    @Autowired
    private MappingService service;

    @Autowired
    private DatabaseService databaseService; // TODO remove later

    @Autowired
    private LogicalModelService logicalModelService;

    @GetMapping("/mappings/{id}")
    public MappingView getMapping(@PathVariable int id) {
        MappingWrapper wrapper = service.find(id);

        if (wrapper == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return wrapperToView(wrapper);
    }

    /**
     * @deprecated
     */
    @Deprecated
    @GetMapping("/schema-categories/{categoryId}/mappings")
    public List<MappingView> getAllMappingsInCategory(@PathVariable int categoryId) {
        return logicalModelService.findAllInCategory(categoryId).stream().flatMap(logicalModel -> {
            return service.findAllInLogicalModel(logicalModel.id).stream();
        })
        .map(this::wrapperToView).toList();
    }

    @GetMapping("/logical-models/{logicalModelId}/mappings")
    public List<MappingView> getAllMappingsInLogicalModel(@PathVariable int logicalModelId) {
        return service.findAllInLogicalModel(logicalModelId).stream().map(this::wrapperToView).toList();
    }

    @PostMapping("/mappings")
    public MappingView createNewMapping(@RequestBody MappingInit newMapping) {
        return wrapperToView(service.createNew(newMapping));
    }

    // TODO this is just sad ...
    private MappingView wrapperToView(MappingWrapper wrapper) {
        LogicalModel logicalModel = logicalModelService.find(wrapper.logicalModelId);
        Database database = databaseService.find(logicalModel.databaseId);
        DatabaseView databaseView = new DatabaseView(database, databaseService.getDatabaseConfiguration(database));
        LogicalModelView logicalModelView = new LogicalModelView(logicalModel, databaseView);
        return new MappingView(wrapper, logicalModelView);
    }

}
