package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceUpdate;
import cz.matfyz.server.entity.datasource.DatasourceResponse;
import cz.matfyz.server.service.DatasourceService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatasourceController {

    @Autowired
    private DatasourceService service;

    @Autowired
    private DatasourceRepository repository;

    @GetMapping("/datasources")
    public List<DatasourceResponse> getAllDatasources(@RequestParam Optional<Id> categoryId) {
        final List<DatasourceEntity> datasources = categoryId.isPresent()
            ? repository.findAllInCategory(categoryId.get())
            : repository.findAll();

        return datasources.stream().map(DatasourceResponse::fromEntity).toList();
    }

    @GetMapping("/datasources/{id}")
    public DatasourceResponse getDatasource(@PathVariable Id id) {
        final DatasourceEntity datasource = repository.find(id);
        return DatasourceResponse.fromEntity(datasource);
    }

    @GetMapping("/datasources/for-mapping")
    public DatasourceResponse getDatasourceForMapping(@RequestParam Id mappingId) {
        final DatasourceEntity datasource = repository.findByMappingId(mappingId);
        return DatasourceResponse.fromEntity(datasource);
    }

    @PostMapping("/datasources")
    public DatasourceResponse createDatasource(@RequestBody DatasourceInit data) {
        final DatasourceEntity datasource = service.create(data);
        return DatasourceResponse.fromEntity(datasource);
    }

    @PutMapping("/datasources/{id}")
    public DatasourceResponse updateDatasource(@PathVariable Id id, @RequestBody DatasourceUpdate update) {
        if (!update.hasPassword()) {
            final var originalDatasource = repository.find(id);
            update.trySetPasswordFrom(originalDatasource);
        }
        final DatasourceEntity datasource = service.update(id, update);

        return DatasourceResponse.fromEntity(datasource);
    }

    @DeleteMapping("/datasources/{id}")
    public void deleteDatasource(@PathVariable Id id) {
        repository.delete(id);
    }

}
