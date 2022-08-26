package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.service.SchemaMorphismService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class SchemaMorphismController {

    @Autowired
    private SchemaMorphismService service;

    @GetMapping("/schemaMorphisms/{id}")
    public SchemaMorphismWrapper getMorphism(@PathVariable int id) {
        SchemaMorphismWrapper morphism = service.find(id);

        if (morphism != null)
            return morphism;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
