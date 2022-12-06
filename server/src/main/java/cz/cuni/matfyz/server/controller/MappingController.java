package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.mapping.MappingFull;
import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
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

    @GetMapping("/mappings/{id}")
    public MappingFull getMapping(@PathVariable int id) {
        var mapping = service.findFull(id);

        if (mapping == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return mapping;
    }

    @GetMapping("/schema-categories/{categoryId}/mappings")
    public List<MappingFull> getAllMappingsInCategory(@PathVariable int categoryId) {
        return service.findAllFullInCategory(categoryId);
    }

    @GetMapping("/logical-models/{logicalModelId}/mappings")
    public List<MappingFull> getAllMappingsInLogicalModel(@PathVariable int logicalModelId) {
        return service.findAllFull(logicalModelId);
    }

    @PostMapping("/mappings")
    public MappingInfo createNewMapping(@RequestBody MappingInit newMapping) {
        return service.createNew(newMapping);
    }

    /*
    private MappingView wrapperToView(MappingWrapper wrapper) {
        LogicalModel logicalModel = logicalModelService.find(wrapper.logicalModelId);
        Database database = databaseService.find(logicalModel.databaseId);
        DatabaseWithConfiguration databaseWithConfiguration = databaseService.findDatabaseWithConfiguration(logicalModel.databaseId)
        LogicalModelFull logicalModelView = new LogicalModelFull(
            logicalModel.id,
            logicalModel.categoryId,
            databaseWithConfiguration,
            logicalModel.jsonValue,

        );
        return new MappingView(wrapper, logicalModelView);
    }
    */

}
