package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.database.DatabaseInit;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseConfiguration;
import cz.cuni.matfyz.server.entity.database.DatabaseUpdate;
import cz.cuni.matfyz.server.entity.database.DatabaseView;
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

    @GetMapping("/database-views")
    public List<DatabaseView> getAllDatabaseViews() {
        return service.findAll().stream().map(database -> new DatabaseView(database, service.getDatabaseConfiguration(database))).toList();
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
    public Database createDatabase(@RequestBody DatabaseInit data) {
        Database database = service.createNew(data);
        if (database == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        database.hidePassword();
        return database;
    }

    @PutMapping("/databases/{id}")
    public Database updateDatabase(@PathVariable int id, @RequestBody DatabaseUpdate update) {
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
