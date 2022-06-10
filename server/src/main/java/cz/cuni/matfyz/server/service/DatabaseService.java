package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.database.CreationData;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.database.UpdateData;
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

    public Database find(int id) {
        return repository.find(id);
    }
    
    public List<Database> findAll() {
        return repository.findAll();
    }

    public Database createNew(CreationData data) {
        var database = new Database(null, data);
        return repository.save(database);
    }

    public Database update(int id, UpdateData data) {
        Database database = repository.find(id);
        if (database == null)
            return null;
        
        database.updateFrom(data);
        return repository.save(database);
    }

    public boolean delete(int id) {
        return repository.delete(id);
    }

}
