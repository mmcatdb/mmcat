package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
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
 * @author jachym.bartik
 */
@RestController
public class MappingController {

    @Autowired
    private MappingService service;

    @GetMapping("/mappings/{id}")
    public MappingWrapper getMapping(@PathVariable Id id) {
        final var mapping = service.find(id);
        if (mapping == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return mapping;
    }

    @GetMapping("/logical-models/{logicalModelId}/mappings")
    public List<MappingWrapper> getAllMappingsInLogicalModel(@PathVariable Id logicalModelId) {
        final var mappings = service.findAll(logicalModelId);
        if (mappings == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return mappings;
    }

    @PostMapping("/mappings")
    public MappingInfo createNewMapping(@RequestBody MappingInit newMapping) {
        return service.createNew(newMapping);
    }

}
