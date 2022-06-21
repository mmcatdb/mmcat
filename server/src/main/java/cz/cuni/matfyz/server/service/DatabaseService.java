package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.database.DatabaseInit;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.DatabaseConfiguration;
import cz.cuni.matfyz.server.entity.database.DatabaseUpdate;
import cz.cuni.matfyz.server.repository.DatabaseRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author jachym.bartik
 */
@Service
public class DatabaseService {

    @Autowired
    private DatabaseRepository repository;

    @Autowired
    private WrapperService wrapperService;

    public Database find(int id) {
        return repository.find(id);
    }
    
    public List<Database> findAll() {
        return repository.findAll();
    }

    public Database createNew(DatabaseInit data) {
        var database = new Database(null, data);
        return repository.save(database);
    }

    public Database update(int id, DatabaseUpdate data) {
        Database database = repository.find(id);
        if (database == null)
            return null;
        
        database.updateFrom(data);
        return repository.save(database);
    }

    public boolean delete(int id) {
        return repository.delete(id);
    }

    public DatabaseConfiguration getDatabaseConfiguration(Database database) {
        return new DatabaseConfiguration(wrapperService.getPathWrapper(database));
    }

}
