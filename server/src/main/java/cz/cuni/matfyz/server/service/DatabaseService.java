package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseConfiguration;
import cz.cuni.matfyz.server.entity.database.DatabaseInit;
import cz.cuni.matfyz.server.entity.database.DatabaseUpdate;
import cz.cuni.matfyz.server.entity.database.DatabaseWithConfiguration;
import cz.cuni.matfyz.server.repository.DatabaseRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class DatabaseService {

    @Autowired
    private DatabaseRepository repository;

    @Autowired
    private WrapperService wrapperService;

    public Database find(int databaseId) {
        return repository.find(databaseId);
    }
    
    public List<Database> findAll() {
        return repository.findAll();
    }

    public Database createNew(DatabaseInit data) {
        var database = new Database(null, data);
        return repository.save(database);
    }

    public Database update(int databaseId, DatabaseUpdate data) {
        Database database = repository.find(databaseId);
        if (database == null)
            return null;
        
        database.updateFrom(data);
        return repository.save(database);
    }

    public boolean delete(int databaseId) {
        return repository.delete(databaseId);
    }

    public DatabaseWithConfiguration findDatabaseWithConfiguration(int databaseId) {
        var database = find(databaseId);
        var configuration = new DatabaseConfiguration(wrapperService.getPathWrapper(database));

        return new DatabaseWithConfiguration(database, configuration);
    }

    public List<DatabaseWithConfiguration> findAllDatabasesWithConfiguration() {
        return findAll().stream().map(database -> {
            var configuration = new DatabaseConfiguration(wrapperService.getPathWrapper(database));
            return new DatabaseWithConfiguration(database, configuration);
        }).toList();
    }
}
