package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.service.SchemaCategoryService;
import cz.cuni.matfyz.server.service.SchemaObjectService;
import cz.cuni.matfyz.server.utils.Position;

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
public class SchemaCategoryController {

    @Autowired
    private SchemaCategoryService service;

    @Autowired
    private SchemaObjectService objectService;

    @GetMapping("/schema-categories")
    public List<SchemaCategoryInfo> getAllCategoryInfos() {
        return service.findAllInfos();
    }

    @PostMapping("/schema-categories")
    public SchemaCategoryInfo createNewSchema(@RequestBody SchemaCategoryInit init) {
        var newInfo = service.createNewInfo(init);
        if (newInfo != null)
            return newInfo;
        
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/schema-categories/{id}/info")
    public SchemaCategoryInfo getCategoryInfo(@PathVariable Id id) {
        SchemaCategoryInfo schema = service.findInfo(id);

        if (schema != null)
            return schema;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/schema-categories/{id}")
    public SchemaCategoryWrapper getCategoryWrapper(@PathVariable Id id) {
        SchemaCategoryWrapper schema = service.find(id);

        if (schema != null)
            return schema;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/schema-categories/{id}/update")
    public SchemaCategoryWrapper updateCategoryWrapper(@RequestBody SchemaCategoryUpdate update) {
        // TOOD
        throw new UnsupportedOperationException();
    }
    

    /*
    @PutMapping("/schema-categories/{id}")
    public SchemaCategoryWrapper updateCategoryWrapper(@PathVariable Id id, @RequestBody SchemaCategoryUpdate update) {
        SchemaCategoryWrapper result = service.update(id, update);
        if (result != null)
            return result;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    */

    /*
    @PutMapping("/schema-categories/{id}/positions")
    public boolean updateCategoryPositions(@PathVariable Id id, @RequestBody PositionUpdate[] positionUpdates) {
        boolean result = true;
        for (PositionUpdate update : positionUpdates)
            result = result && objectService.updatePosition(id, update.schemaObjectId, update.position);
        
        if (result)
            return true;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    */

    static record PositionUpdate(
        //int schemaObjectId,
        Id schemaObjectId,
        Position position
    ) {}

    /*
    @GetMapping("/schema-categories/{id}/mapping-options")
    public MappingOptionsView getMappingOptions(@PathVariable Id id) {
        return new MappingOptionsView(id, databaseService.findAll());
    }
    */
}
