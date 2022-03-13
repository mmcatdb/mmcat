package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.service.SchemaCategoryService;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/schemaCategories")
    public List<SchemaCategoryInfo> getAllCategoryInfos()
    {
        return service.findAllInfos();
    }

    @GetMapping("/schemaCategories/{id}")
    //public IdentifiedSchemaCategory getCategoryById(@PathVariable String id) // TODO
    public SchemaCategoryWrapper getCategoryWrapperById(@PathVariable int id)
    {
        SchemaCategoryWrapper schema = service.findWrapper(id);

        if (schema != null)
            return schema;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
