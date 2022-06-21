package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.service.SchemaObjectService;

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
public class SchemaObjectController
{
    @Autowired
    private SchemaObjectService service;

    @GetMapping("/schemaObjects/{id}")
    public SchemaObjectWrapper getObject(@PathVariable int id)
    {
        SchemaObjectWrapper object = service.find(id);

        if (object != null)
            return object;

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
