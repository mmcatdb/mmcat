package cz.matfyz.server.controller;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SchemaCategoryController {

    @Autowired
    private SchemaCategoryRepository repository;

    @Autowired
    private SchemaCategoryService service;

    public record SchemaCategoryInfo(
        Id id,
        Version version,
        Version lastValid,
        String label,
        /** The current version of the whole project. */
        Version systemVersion
    ) implements IEntity {

        public static SchemaCategoryInfo fromWrapper(SchemaCategoryWrapper wrapper) {
            return new SchemaCategoryInfo(wrapper.id(), wrapper.version(), wrapper.lastValid(), wrapper.label, wrapper.systemVersion());
        }

    }

    @GetMapping("/schema-categories")
    public List<SchemaCategoryInfo> getAllCategoryInfos() {
        return repository.findAllInfos();
    }

    public record SchemaCategoryInit(
        String label
    ) {}

    @PostMapping("/schema-categories")
    public SchemaCategoryInfo createNewCategory(@RequestBody SchemaCategoryInit init) {
        return SchemaCategoryInfo.fromWrapper(service.create(init.label));
    }

    @GetMapping("/schema-categories/{id}/info")
    public SchemaCategoryInfo getCategoryInfo(@PathVariable Id id) {
        return repository.findInfo(id);
    }

    @GetMapping("/schema-categories/{id}")
    public SchemaCategoryWrapper getCategory(@PathVariable Id id) {
        return repository.find(id);
    }

    public record SchemaCategoryStats(
        int objexes,
        int mappings,
        int jobs
    ) {}

    @GetMapping("/schema-categories/{id}/stats")
    public SchemaCategoryStats getCategoryStats(@PathVariable Id id) {
        return repository.findStats(id);
    }

    @PostMapping("/schema-categories/{id}/updates")
    public SchemaCategoryWrapper updateCategory(@PathVariable Id id, @RequestBody SchemaEvolutionInit update) {
        return service.update(id, update);
    }

    @DeleteMapping("/schema-categories/{id}")
    public void deleteCategory(@PathVariable Id id) {
        repository.delete(id);
    }

}
