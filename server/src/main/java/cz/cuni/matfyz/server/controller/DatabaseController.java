package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.database.CreationData;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseConfiguration;
import cz.cuni.matfyz.server.entity.database.UpdateData;
import cz.cuni.matfyz.server.entity.database.View;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.WrapperService;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class DatabaseController {

    private static Logger LOGGER = LoggerFactory.getLogger(DatabaseController.class);

    @Autowired
    private DatabaseService service;

    @Autowired
    private WrapperService wrapperService;

    @GetMapping("/database-views")
    public List<View> getAllDatabaseViews() {
        return service.findAll().stream().map(database -> createDatabaseView(database)).toList();
    }

    private View createDatabaseView(Database database) {
        var configuration = new DatabaseConfiguration(wrapperService.getPathWrapper(database));
        return new View(database, configuration);
    }

    @GetMapping("/databases")
    public List<Database> getAllDatabases() {
        var databases = service.findAll();
        databases.stream().forEach(database -> database.hidePassword());
        return databases;
    }

    @GetMapping("/databases/{id}")
    public Database getDatabase(@PathVariable int id) {
        Database database = service.find(id);
        if (database == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        
        database.hidePassword();
        return database;
    }

    @PostMapping("/databases")
    public Database createDatabase(@RequestBody CreationData data) {
        Database database = service.createNew(data);
        if (database == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        database.hidePassword();
        return database;
    }

    @PutMapping("/databases/{id}")
    public Database updateDatabase(@PathVariable int id, @RequestBody UpdateData update) {
        if (!update.hasPassword()) {
            var originalDatabase = service.find(id);
            update.setPasswordFrom(originalDatabase);
        }
        Database database = service.update(id, update);
        if (database == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        database.hidePassword();
        return database;
    }

    @DeleteMapping("/databases/{id}")
    public void deleteDatabase(@PathVariable int id) {
        boolean status = service.delete(id);
        if (!status)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The database can't be deleted. Check that there aren't any mappings that depend on it.");
    }

}
