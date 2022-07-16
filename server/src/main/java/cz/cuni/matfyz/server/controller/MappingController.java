package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseView;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingView;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.service.DatabaseService;
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
 * 
 * @author jachym.bartik
 */
@RestController
public class MappingController {

    @Autowired
    private MappingService service;

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/mappings/{id}")
    public MappingView getMapping(@PathVariable int id) {
        MappingWrapper wrapper = service.find(id);

        if (wrapper == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return wrapperToView(wrapper);
    }

    @GetMapping("/schema/{schemaId}/mappings")
    public List<MappingView> getAllMappingsInCategory(@PathVariable int schemaId) {
        return service.findAllInCategory(schemaId).stream().map(wrapper -> wrapperToView(wrapper)).toList();
    }

    @PostMapping("/mappings")
    public MappingView createNewMapping(@RequestBody MappingInit newMapping) {
        return wrapperToView(service.createNew(newMapping));
    }

    private MappingView wrapperToView(MappingWrapper wrapper) {
        Database database = databaseService.find(wrapper.databaseId);
        DatabaseView databaseView = new DatabaseView(database, databaseService.getDatabaseConfiguration(database));
        return new MappingView(wrapper, databaseView);
    }

}
