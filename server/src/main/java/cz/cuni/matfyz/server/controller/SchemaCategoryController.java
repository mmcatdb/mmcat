package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfoBetter;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
@RestController
public class SchemaCategoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaCategoryController.class);

    @Autowired
    private SchemaCategoryService service;

    @Autowired
    private SchemaObjectService objectService;

    @GetMapping("/schema-categories")
    public List<SchemaCategoryInfoBetter> getAllCategoryInfos() {
        return service.findAllInfos().stream().map(info -> new SchemaCategoryInfoBetter(info)).toList();
    }

    @PostMapping("/schema-categories")
    public SchemaCategoryInfo createNewSchema(@RequestBody SchemaCategoryInit init) {
        var newInfo = service.createNewInfo(init);
        if (newInfo != null)
            return newInfo;
        
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/schema-categories/{id}/info")
    public SchemaCategoryInfo getCategoryInfo(@PathVariable int id) {
        SchemaCategoryInfo schema = service.findInfo(id);

        if (schema != null)
            return schema;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/schema-categories/{id}")
    public SchemaCategoryWrapper getCategoryWrapper(@PathVariable int id) {
        SchemaCategoryWrapper schema = service.find(id);

        if (schema != null)
            return schema;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/schema-categories/{id}")
    public SchemaCategoryWrapper updateCategoryWrapper(@PathVariable int id, @RequestBody SchemaCategoryUpdate update) {
        SchemaCategoryWrapper result = service.update(id, update);
        if (result != null)
            return result;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/schema-categories/{id}/positions")
    public boolean updateCategoryPositions(@PathVariable int id, @RequestBody PositionUpdate[] positionUpdates) {
        boolean result = true;
        for (PositionUpdate update : positionUpdates) {
            var parsed = Integer.parseInt(update.schemaObjectId.value);
            result = result && objectService.updatePosition(id, parsed, update.position);
            LOGGER.info(update.toString());
            LOGGER.info("" + parsed);
        }
        
        if (result)
            return true;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    static record PositionUpdate(
        //int schemaObjectId,
        Id schemaObjectId,
        Position position
    ) {}

    /*
    @GetMapping("/schema-categories/{id}/mapping-options")
    public MappingOptionsView getMappingOptions(@PathVariable int id) {
        return new MappingOptionsView(id, databaseService.findAll());
    }
    */
}
