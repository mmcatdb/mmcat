package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSource;
import cz.matfyz.server.entity.datasource.DataSourceInit;
import cz.matfyz.server.entity.datasource.DataSourceUpdate;
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

    @GetMapping("/data-sources")
    public List<DataSource> getAllDataSources(@RequestParam Optional<Id> categoryId) {
        return categoryId.isPresent() ? service.findAllInCategory(categoryId.get()) : service.findAll();
    }

    @GetMapping("/data-sources/{id}")
    public DataSource getDataSource(@PathVariable Id id) {
        DataSource dataSource = service.find(id);
        if (dataSource == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return dataSource;
    }

    @PostMapping("/data-sources")
    public DataSource createDataSource(@RequestBody DataSourceInit data) {
        DataSource dataSource = service.createNew(data);
        if (dataSource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return dataSource;
    }

    @PutMapping("/data-sources/{id}")
    public DataSource updateDataSource(@PathVariable Id id, @RequestBody DataSourceUpdate update) {
        DataSource dataSource = service.update(id, update);
        if (dataSource == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return dataSource;
    }

    @DeleteMapping("/data-sources/{id}")
    public void deleteDataSource(@PathVariable Id id) {
        boolean status = service.delete(id);
        if (!status)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The data source can't be deleted. Check that there aren't any jobs that depend on it.");
    }

}
