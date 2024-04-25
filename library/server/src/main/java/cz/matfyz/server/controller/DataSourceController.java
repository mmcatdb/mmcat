package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSourceEntity;
import cz.matfyz.server.entity.datasource.DataSourceInfo;
import cz.matfyz.server.entity.datasource.DataSourceInit;
import cz.matfyz.server.entity.datasource.DataSourceUpdate;
import cz.matfyz.server.entity.datasource.DataSourceWithConfiguration;
import cz.matfyz.server.service.DataSourceService;

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
public class DataSourceController {

    @Autowired
    private DataSourceService service;

    @GetMapping("/data-source-infos")
    public List<DataSourceWithConfiguration> getAllDataSourceInfos() {
        return service.findAllDataSourcesWithConfiguration();
    }

    @GetMapping("/data-sources")
    public List<DataSourceEntity> getAllDataSources(@RequestParam Optional<Id> categoryId) {
        var dataSources = categoryId.isPresent() ? service.findAllInCategory(categoryId.get()) : service.findAll();
        dataSources.forEach(DataSourceEntity::hidePassword);
        return dataSources;
    }

    @GetMapping("/data-sources/{id}")
    public DataSourceEntity getDataSource(@PathVariable Id id) {
        DataSourceEntity dataSource = service.find(id);
        if (dataSource == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        dataSource.hidePassword();
        return dataSource;
    }

    @PostMapping("/data-sources")
    public DataSourceInfo createDataSource(@RequestBody DataSourceInit data) {
        DataSourceEntity dataSource = service.createNew(data);
        if (dataSource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return dataSource.toInfo();
    }

    @PutMapping("/data-sources/{id}")
    public DataSourceInfo updateDataSource(@PathVariable Id id, @RequestBody DataSourceUpdate update) {
        if (!update.hasPassword()) {
            var originalDataSource = service.find(id);
            update.setPasswordFrom(originalDataSource);
        }
        DataSourceEntity dataSource = service.update(id, update);
        if (dataSource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return dataSource.toInfo();
    }

    @DeleteMapping("/data-sources/{id}")
    public void deleteDataSource(@PathVariable Id id) {
        boolean status = service.delete(id);
        if (!status)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The data source can't be deleted. Check that there aren't any mappings that depend on it.");
    }

}

