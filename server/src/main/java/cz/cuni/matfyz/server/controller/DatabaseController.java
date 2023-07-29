package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseInit;
import cz.cuni.matfyz.server.entity.database.DatabaseUpdate;
import cz.cuni.matfyz.server.entity.database.DatabaseWithConfiguration;
import cz.cuni.matfyz.server.service.DatabaseService;

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
public class DatabaseController {

    @Autowired
    private DatabaseService service;

    @GetMapping("/database-infos")
    public List<DatabaseWithConfiguration> getAllDatabaseInfos() {
        return service.findAllDatabasesWithConfiguration();
    }

    @GetMapping("/databases")
    public List<Database> getAllDatabases(@RequestParam Optional<Id> categoryId) {
        var databases = categoryId.isPresent() ? service.findAllInCategory(categoryId.get()) : service.findAll();
        databases.forEach(Database::hidePassword);
        return databases;
    }

    @GetMapping("/databases/{id}")
    public Database getDatabase(@PathVariable Id id) {
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
    public Database updateDatabase(@PathVariable Id id, @RequestBody DatabaseUpdate update) {
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
    public void deleteDatabase(@PathVariable Id id) {
        boolean status = service.delete(id);
        if (!status)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The database can't be deleted. Check that there aren't any mappings that depend on it.");
    }

}
