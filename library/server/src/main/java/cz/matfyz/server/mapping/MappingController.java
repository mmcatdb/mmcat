package cz.matfyz.server.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.utils.entity.IEntity;
import cz.matfyz.server.utils.entity.Id;

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
    public MappingEntity getMapping(@PathVariable Id id) {
        return repository.find(id);
    }

    @GetMapping("/mappings/all")
    public List<MappingEntity> getAllMappings() {
        return repository.findAll();
    }

    @GetMapping("/mappings")
    public List<MappingEntity> getAllMappingsInCategory(@RequestParam Id categoryId, @RequestParam Optional<Id> datasourceId) {
        return repository.findAllInCategory(categoryId, datasourceId.orElse(null));
    }

    public record MappingInfo(
        Id id,
        String kindName,
        Version version
    ) implements IEntity {

        public static MappingInfo fromEntity(MappingEntity mappingEntity) {
            return new MappingInfo(mappingEntity.id(), mappingEntity.kindName, mappingEntity.version());
        }

    }

    @PostMapping("/mappings")
    public MappingEntity createMapping(@RequestBody MappingInit newMapping) {
        return service.create(newMapping);
    }


    public record MappingInit(
        Id categoryId,
        Id datasourceId,
        Key rootObjexKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) {

        public static MappingInit fromMapping(Mapping mapping, Id categoryId, Id datasourceId) {
            return new MappingInit(
                categoryId,
                datasourceId,
                mapping.rootObjex().key(),
                mapping.primaryKey().stream().toList(),
                mapping.kindName(),
                mapping.accessPath()
            );
        }

    }

    @PostMapping("/mappings/{id}")
    public MappingEntity updateMapping(@RequestParam Id id, @RequestBody MappingEdit update) {
        final var mapping = repository.find(id);
        service.update(mapping, update);

        return mapping;
    }

    public record MappingEdit(
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) {}

}
