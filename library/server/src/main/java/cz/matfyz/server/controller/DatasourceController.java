package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceUpdate;
import cz.matfyz.server.entity.datasource.DatasourceDetail;
import cz.matfyz.server.service.DatasourceService;
import cz.matfyz.server.service.WrapperService;

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

    @Autowired
    private WrapperService wrapperService;

    @GetMapping("/datasources")
    public List<DatasourceDetail> getAllDatasources(@RequestParam Optional<Id> categoryId) {
        final List<DatasourceWrapper> datasources = categoryId.isPresent()
            ? repository.findAllInCategory(categoryId.get())
            : repository.findAll();

        return datasources.stream().map(this::datasourceToDetail).toList();
    }

    @GetMapping("/datasources/{id}")
    public DatasourceDetail getDatasource(@PathVariable Id id) {
        final DatasourceWrapper datasource = repository.find(id);
        return datasourceToDetail(datasource);
    }

    @PostMapping("/datasources")
    public DatasourceDetail createDatasource(@RequestBody DatasourceInit data) {
        final DatasourceWrapper datasource = service.create(data);
        return datasourceToDetail(datasource);
    }

    @PutMapping("/datasources/{id}")
    public DatasourceDetail updateDatasource(@PathVariable Id id, @RequestBody DatasourceUpdate update) {
        if (!update.hasPassword()) {
            final var originalDatasource = repository.find(id);
            update.setPasswordFrom(originalDatasource);
        }
        final DatasourceWrapper datasource = service.update(id, update);

        return datasourceToDetail(datasource);
    }

    @DeleteMapping("/datasources/{id}")
    public void deleteDatasource(@PathVariable Id id) {
        repository.delete(id);
    }

    public DatasourceDetail datasourceToDetail(DatasourceWrapper datasource) {
        datasource.hidePassword();
        return DatasourceDetail.create(datasource, wrapperService.getControlWrapper(datasource));
    }

}

