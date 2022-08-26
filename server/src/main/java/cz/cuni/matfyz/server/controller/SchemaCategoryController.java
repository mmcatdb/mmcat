package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryUpdate;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.SchemaCategoryService;
import cz.cuni.matfyz.server.service.SchemaObjectService;
import cz.cuni.matfyz.server.utils.Position;
import cz.cuni.matfyz.server.view.MappingOptionsView;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class SchemaCategoryController {

    @Autowired
    private SchemaCategoryService service;

    @Autowired
    private SchemaObjectService objectService;

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/schemaCategories")
    public List<SchemaCategoryInfo> getAllCategoryInfos() {
        return service.findAllInfos();
    }

    @PostMapping("/schemaCategories")
    public SchemaCategoryInfo createNewSchema(@RequestBody SchemaCategoryInit init) {
        var newInfo = service.createNewInfo(init);
        if (newInfo != null)
            return newInfo;
        
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/schemaCategories/{id}")
    public SchemaCategoryWrapper getCategoryWrapper(@PathVariable int id) {
        SchemaCategoryWrapper schema = service.find(id);

        if (schema != null)
            return schema;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/schemaCategories/{id}")
    public SchemaCategoryWrapper updateCategoryWrapper(@PathVariable int id, @RequestBody SchemaCategoryUpdate update) {
        SchemaCategoryWrapper result = service.update(id, update);
        if (result != null)
            return result;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/schemaCategories/positions/{id}")
    public boolean updateCategoryPositions(@PathVariable int id, @RequestBody PositionUpdate[] positionUpdates) {
        boolean result = true;
        for (PositionUpdate update : positionUpdates)
            result = result && objectService.updatePosition(id, update.schemaObjectId, update.position);
        
        if (result)
            return true;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    static record PositionUpdate(
        int schemaObjectId,
        Position position
    ) {}

    @GetMapping("/schemaCategories/{id}/mappingOptions")
    public MappingOptionsView getMappingOptions(@PathVariable int id) {
        return new MappingOptionsView(id, databaseService.findAll());
    }
}
