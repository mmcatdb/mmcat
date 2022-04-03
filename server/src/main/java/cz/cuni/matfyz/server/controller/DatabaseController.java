package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.service.DatabaseService;
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
public class DatabaseController
{
    @Autowired
    private DatabaseService service;

    @GetMapping("/databases")
    public List<DatabaseView> getAllDatabases()
    {
        return service.findAll().stream().map(database -> new DatabaseView(database)).toList();
    }

    @GetMapping("/databases/{id}")
    public DatabaseView getDatabaseById(@PathVariable Integer id)
    {
        Database database = service.find(id);
        if (database != null)
            return new DatabaseView(database);
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
