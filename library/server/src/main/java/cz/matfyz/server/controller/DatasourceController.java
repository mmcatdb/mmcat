package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceUpdate;
import cz.matfyz.server.entity.datasource.DatasourceDetail;
import cz.matfyz.server.service.DatasourceService;
import cz.matfyz.server.service.WrapperService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class DatasourceController {

    @Autowired
    private DatasourceService service;

    @Autowired
    private WrapperService wrapperService;

    @GetMapping("/datasources")
    public List<DatasourceDetail> getAllDatasources(@RequestParam Optional<Id> categoryId) {
        final var datasources = categoryId.isPresent() ? service.findAllInCategory(categoryId.get()) : service.findAll();
        datasources.forEach(DatasourceWrapper::hidePassword);
        return datasources.stream().map(this::datasourceToDetail).toList();
    }

    @GetMapping("/datasources/{id}")
    public DatasourceDetail getDatasource(@PathVariable Id id) {
        final DatasourceWrapper datasource = service.find(id);
        if (datasource == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        datasource.hidePassword();
        return datasourceToDetail(datasource);
    }

    @PostMapping("/datasources")
    public DatasourceDetail createDatasource(@RequestBody DatasourceInit data) {
        final DatasourceWrapper datasource = service.createNew(data);
        if (datasource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        datasource.hidePassword();

        return datasourceToDetail(datasource);
    }

    @PutMapping("/datasources/{id}")
    public DatasourceDetail updateDatasource(@PathVariable Id id, @RequestBody DatasourceUpdate update) {
        if (!update.hasPassword()) {
            final var originalDatasource = service.find(id);
            update.setPasswordFrom(originalDatasource);
        }
        final DatasourceWrapper datasource = service.update(id, update);
        if (datasource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        datasource.hidePassword();

        return datasourceToDetail(datasource);
    }

    @DeleteMapping("/datasources/{id}")
    public void deleteDatasource(@PathVariable Id id) {
        final boolean status = service.delete(id);
        if (!status)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The data source can't be deleted. Check that there aren't any mappings that depend on it.");
    }

    public DatasourceDetail datasourceToDetail(DatasourceWrapper datasource) {
        return DatasourceDetail.create(datasource, wrapperService.getControlWrapper(datasource));
    }

}

