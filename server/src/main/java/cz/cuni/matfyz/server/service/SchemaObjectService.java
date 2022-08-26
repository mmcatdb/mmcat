package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.SchemaObjectRepository;
import cz.cuni.matfyz.server.utils.Position;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class SchemaObjectService {

    @Autowired
    private SchemaObjectRepository repository;

    public List<SchemaObjectWrapper> findAllInCategory(int categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public SchemaObjectWrapper find(int id) {
        return repository.find(id);
    }

    public boolean updatePosition(int categoryId, int objectId, Position newPosition) {
        return repository.updatePosition(categoryId, objectId, newPosition);
    }

    /*
    public SchemaObjectWrapper add(SchemaObjectWrapper object, int categoryId) {
        Integer generatedId = repository.add(object, categoryId);

        return generatedId == null ? null : new SchemaObjectWrapper(generatedId, object.jsonValue, object.position);
    }
    */
}
