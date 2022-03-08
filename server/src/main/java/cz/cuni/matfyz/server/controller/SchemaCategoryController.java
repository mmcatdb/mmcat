package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.IdentifiedSchemaCategory;
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
    private SchemaCategoryService categoryService;

    @GetMapping("/schemaCategories")
    public List<IdentifiedSchemaCategory> getAllCategories()
    {
        return categoryService.findAll();
    }

    @GetMapping("/schemaCategories/{id}")
    //public IdentifiedSchemaCategory getCategoryById(@PathVariable String id) // TODO
    public String getCategoryById(@PathVariable String id)
    {
        IdentifiedSchemaCategory schema = categoryService.find(id);
        if (schema != null)
            return schema.category.toJSON().toString();
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
