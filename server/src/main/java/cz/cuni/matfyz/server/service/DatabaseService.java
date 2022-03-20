package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.DatabaseRepository;
import cz.cuni.matfyz.server.entity.Database;

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
    
}
