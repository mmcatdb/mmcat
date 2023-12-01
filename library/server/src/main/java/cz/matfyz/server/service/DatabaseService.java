package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.entity.database.DatabaseConfiguration;
import cz.matfyz.server.entity.database.DatabaseInit;
import cz.matfyz.server.entity.database.DatabaseUpdate;
import cz.matfyz.server.entity.database.DatabaseWithConfiguration;
import cz.matfyz.server.repository.DatabaseRepository;

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

    public DatabaseEntity find(Id databaseId) {
        return repository.find(databaseId);
    }
    
    public List<DatabaseEntity> findAll() {
        return repository.findAll();
    }

    public List<DatabaseEntity> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public DatabaseEntity createNew(DatabaseInit data) {
        var database = new DatabaseEntity(null, data);
        return repository.save(database);
    }

    public DatabaseEntity update(Id databaseId, DatabaseUpdate data) {
        DatabaseEntity database = repository.find(databaseId);
        if (database == null)
            return null;
        
        database.updateFrom(data);
        return repository.save(database);
    }

    public boolean delete(Id databaseId) {
        return repository.delete(databaseId);
    }

    public DatabaseWithConfiguration findDatabaseWithConfiguration(Id databaseId) {
        var database = find(databaseId);
        var configuration = new DatabaseConfiguration(wrapperService.getControlWrapper(database).getPathWrapper());

        return new DatabaseWithConfiguration(database, configuration);
    }

    public List<DatabaseWithConfiguration> findAllDatabasesWithConfiguration() {
        return findAll().stream().map(database -> {
            var configuration = new DatabaseConfiguration(wrapperService.getControlWrapper(database).getPathWrapper());
            return new DatabaseWithConfiguration(database, configuration);
        }).toList();
    }
}
