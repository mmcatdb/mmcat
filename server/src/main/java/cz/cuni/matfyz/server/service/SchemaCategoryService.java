package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.SchemaCategoryRepository;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.server.entity.IdentifiedSchemaCategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * 
 * @author jachym.bartik
 */
@Service
public class SchemaCategoryService
{
    @Autowired
    private SchemaCategoryRepository repository;

    private Map<String, IdentifiedSchemaCategory> schemas;

    private void loadCategories()
    {
        schemas = new TreeMap<>();
        var schemaList = repository.findAll();
        schemaList.stream().forEach(schema -> schemas.put(schema.id, schema));
    }

    public List<IdentifiedSchemaCategory> findAll()
    {
        if (schemas == null)
            loadCategories();
        
        return schemas.values().stream().toList();
    }

    public IdentifiedSchemaCategory find(String id)
    {
        if (schemas == null)
            loadCategories();

        return schemas.get(id);
    }

    public String add(SchemaCategory schema)
    {
        return repository.add(schema);
    }
}
