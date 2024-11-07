package cz.matfyz.server.controller;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.mapping.MappingUpdate;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.service.MappingService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MappingController {

    @Autowired
    private MappingRepository repository;

    @Autowired
    private MappingService service;

    @GetMapping("/mappings/{id}")
    public MappingWrapper getMapping(@PathVariable Id id) {
        return repository.find(id);
    }

    @GetMapping("/mappings")
    public List<MappingWrapper> getAllMappingsInCategory(@RequestParam Id categoryId, @RequestParam Optional<Id> datasourceId) {
        return repository.findAllInCategory(categoryId, datasourceId.orElse(null));
    }

    public record MappingInfo(
        Id id,
        String kindName,
        Version version
    ) implements IEntity {

        public static MappingInfo fromWrapper(MappingWrapper wrapper) {
            return new MappingInfo(wrapper.id(), wrapper.kindName, wrapper.version());
        }

    }

    @PostMapping("/mappings")
    public MappingWrapper createMapping(@RequestBody MappingInit newMapping) {
        return service.create(newMapping);
    }

    @PostMapping("/mappings/{id}/update")
    public MappingWrapper updateMapping(@RequestBody MappingUpdate update) {
        // TOOD
        throw new UnsupportedOperationException();
    }

}
