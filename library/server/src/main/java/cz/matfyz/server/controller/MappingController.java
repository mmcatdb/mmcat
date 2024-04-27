package cz.matfyz.server.controller;

import cz.matfyz.evolution.mapping.MappingUpdate;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.service.MappingService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MappingController {

    @Autowired
    private MappingService service;

    @GetMapping("/mappings/{id}")
    public MappingWrapper getMapping(@PathVariable Id id) {
        return service.find(id);
    }

    @GetMapping("/logical-models/{logicalModelId}/mappings")
    public List<MappingWrapper> getAllMappingsInLogicalModel(@PathVariable Id logicalModelId) {
        return service.findAll(logicalModelId);
    }

    @PostMapping("/mappings")
    public MappingInfo createNewMapping(@RequestBody MappingInit newMapping) {
        return service.createNew(newMapping);
    }

    @PostMapping("/mappings/{id}/update")
    public MappingWrapper updateCategoryWrapper(@RequestBody MappingUpdate update) {
        // TOOD
        throw new UnsupportedOperationException();
    }

}
