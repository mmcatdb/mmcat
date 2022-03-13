package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.SchemaObjectRepository;
import cz.cuni.matfyz.server.entity.SchemaObjectWrapper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author jachym.bartik
 */
@Service
public class SchemaObjectService
{
    @Autowired
    private SchemaObjectRepository repository;

    public List<SchemaObjectWrapper> findAllInCategory(int categoryId)
    {
        return repository.findAllInCategory(categoryId);
    }

    public SchemaObjectWrapper find(int id)
    {
        return repository.find(id);
    }
}
