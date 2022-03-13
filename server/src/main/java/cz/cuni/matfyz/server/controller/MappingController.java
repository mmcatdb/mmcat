package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.MappingWrapper;
import cz.cuni.matfyz.server.service.MappingService;

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
public class MappingController
{
    @Autowired
    private MappingService service;

    @GetMapping("/mappings/{id}")
    public MappingWrapper getObjectById(@PathVariable int id) // TODO
    //public String getObjectById(@PathVariable int id)
    {
        MappingWrapper object = service.find(id);

        if (object != null)
            //return object.toJSON().toString();
            return object;

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
