package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.server.service.SchemaCategoryService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/schema-categories")
    public List<SchemaCategoryInfo> getAllCategoryInfos() {
        return repository.findAllInfos();
    }

    @PostMapping("/schema-categories")
    public SchemaCategoryInfo createNewCategory(@RequestBody SchemaCategoryInit init) {
        if (init.label() == null)
            return null;

        return SchemaCategoryInfo.fromWrapper(service.create(init));
    }

    @GetMapping("/schema-categories/{id}/info")
    public SchemaCategoryInfo getCategoryInfo(@PathVariable Id id) {
        return repository.findInfo(id);
    }

    @GetMapping("/schema-categories/{id}")
    public SchemaCategoryWrapper getCategoryWrapper(@PathVariable Id id) {
        return repository.find(id);
    }

    @PostMapping("/schema-categories/{id}/updates")
    public SchemaCategoryWrapper updateCategoryWrapper(@PathVariable Id id, @RequestBody SchemaUpdateInit update) {
        return service.update(id, update);
    }

}
