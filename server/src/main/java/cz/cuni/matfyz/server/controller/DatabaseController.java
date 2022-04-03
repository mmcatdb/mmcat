package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.WrapperService;
import cz.cuni.matfyz.server.view.DatabaseConfiguration;
import cz.cuni.matfyz.server.view.DatabaseView;
import cz.cuni.matfyz.server.entity.Database;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class DatabaseController {

    @Autowired
    private DatabaseService service;

    @Autowired
    private WrapperService wrapperService;

    @GetMapping("/databases")
    public List<DatabaseView> getAllDatabases() {
        var output = service.findAll().stream().map(database -> createDatabaseView(database)).toList();
        return output;
    }

    @GetMapping("/databases/{id}")
    public DatabaseView getDatabaseById(@PathVariable Integer id) {
        Database database = service.find(id);
        if (database != null)
            return createDatabaseView(database);
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private DatabaseView createDatabaseView(Database database) {
        var configuration = new DatabaseConfiguration(wrapperService.getPathWrapper(database));
        return new DatabaseView(database, configuration);
    }

}
