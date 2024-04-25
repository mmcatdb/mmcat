package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.datasource.DatasourceInfo;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceUpdate;
import cz.matfyz.server.entity.datasource.DatasourceWithConfiguration;
import cz.matfyz.server.service.DatasourceService;

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

    @GetMapping("/data-source-infos")
    public List<DatasourceWithConfiguration> getAllDatasourceInfos() {
        return service.findAllDatasourcesWithConfiguration();
    }

    @GetMapping("/data-sources")
    public List<DatasourceWrapper> getAllDatasources(@RequestParam Optional<Id> categoryId) {
        var datasources = categoryId.isPresent() ? service.findAllInCategory(categoryId.get()) : service.findAll();
        datasources.forEach(DatasourceWrapper::hidePassword);
        return datasources;
    }

    @GetMapping("/data-sources/{id}")
    public DatasourceWrapper getDatasource(@PathVariable Id id) {
        DatasourceWrapper datasource = service.find(id);
        if (datasource == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        datasource.hidePassword();
        return datasource;
    }

    @PostMapping("/data-sources")
    public DatasourceInfo createDatasource(@RequestBody DatasourceInit data) {
        DatasourceWrapper datasource = service.createNew(data);
        if (datasource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return datasource.toInfo();
    }

    @PutMapping("/data-sources/{id}")
    public DatasourceInfo updateDatasource(@PathVariable Id id, @RequestBody DatasourceUpdate update) {
        if (!update.hasPassword()) {
            var originalDatasource = service.find(id);
            update.setPasswordFrom(originalDatasource);
        }
        DatasourceWrapper datasource = service.update(id, update);
        if (datasource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return datasource.toInfo();
    }

    @DeleteMapping("/data-sources/{id}")
    public void deleteDatasource(@PathVariable Id id) {
        boolean status = service.delete(id);
        if (!status)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The data source can't be deleted. Check that there aren't any mappings that depend on it.");
    }

}

