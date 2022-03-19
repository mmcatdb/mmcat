package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.Position;
import cz.cuni.matfyz.server.entity.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.service.SchemaCategoryService;
import cz.cuni.matfyz.server.service.SchemaObjectService;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class SchemaCategoryController
{
    @Autowired
    private SchemaCategoryService service;

    @Autowired
    private SchemaObjectService objectService;

    @GetMapping("/schemaCategories")
    public List<SchemaCategoryInfo> getAllCategoryInfos()
    {
        return service.findAllInfos();
    }

    @GetMapping("/schemaCategories/{id}")
    //public IdentifiedSchemaCategory getCategoryById(@PathVariable String id) // TODO
    public SchemaCategoryWrapper getCategoryWrapperById(@PathVariable int id)
    {
        SchemaCategoryWrapper schema = service.find(id);

        if (schema != null)
            return schema;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/schemaCategories/positions/{id}")
    public boolean updateCategoryPositions(@PathVariable int id, @RequestBody PositionUpdate[] positionUpdates)
    {
        boolean result = true;
        for (PositionUpdate update : positionUpdates)
            result = result && objectService.updatePosition(id, update.schemaObjectId, update.position);
        
        if (result)
            return true;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    static class PositionUpdate
    {
        public int schemaObjectId;
        public Position position;

        //public PositionUpdate() {}
    }
}
