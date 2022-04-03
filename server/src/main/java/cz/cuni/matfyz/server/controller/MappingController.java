package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.MappingWrapper;
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

    @GetMapping("/mappings/{id}")
    public MappingWrapper getMappingById(@PathVariable int id) {
        MappingWrapper object = service.find(id);

        if (object != null)
            return object;

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/mappings")
    public List<MappingWrapper> getAllMappings() {
        // TODO
        return service.findAllInCategory(1);
    }

    @PostMapping("/mappings")
    public MappingWrapper createNewMapping(@RequestBody MappingWrapper newMapping) {
        return service.createNew(newMapping);
    }

}
