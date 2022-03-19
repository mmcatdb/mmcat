package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.SchemaCategoryRepository;
import cz.cuni.matfyz.server.repository.SchemaMorphismRepository;
import cz.cuni.matfyz.server.repository.SchemaObjectRepository;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.server.entity.Position;
import cz.cuni.matfyz.server.entity.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.entity.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.entity.SchemaObjectWrapper;

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

    @Autowired
    private SchemaObjectRepository objectRepository;

    @Autowired
    private SchemaMorphismRepository morphismRepository;

    public List<SchemaCategoryInfo> findAllInfos()
    {
        return repository.findAll();
    }

    public SchemaCategoryWrapper find(int id)
    {
        SchemaCategoryInfo info = repository.find(id);
        if (info == null)
            return null;
            
        List<SchemaObjectWrapper> objects = objectRepository.findAllInCategory(id);
        List<SchemaMorphismWrapper> morphisms = morphismRepository.findAllInCategory(id);

        return new SchemaCategoryWrapper(info, objects, morphisms);
    }

    public Integer add(SchemaCategory schema)
    {
        return repository.add(schema);
    }
}
