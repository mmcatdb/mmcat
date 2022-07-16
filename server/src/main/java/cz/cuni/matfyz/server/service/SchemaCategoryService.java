package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryUpdate;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.SchemaCategoryRepository;
import cz.cuni.matfyz.server.repository.SchemaMorphismRepository;
import cz.cuni.matfyz.server.repository.SchemaObjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jachym.bartik
 */
@Service
public class SchemaCategoryService
{
    private static Logger LOGGER = LoggerFactory.getLogger(SchemaCategoryService.class);

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

    public SchemaCategoryInfo createNewInfo(SchemaCategoryInit init)
    {
        Integer generatedId = repository.add(init);

        return generatedId == null ? null : new SchemaCategoryInfo(generatedId, init.jsonValue);
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

    public SchemaCategoryWrapper update(int id, SchemaCategoryUpdate update)
    {
        var temporaryIdMap = new TreeMap<Integer, Integer>();
        for (var object : update.objects) {
            var generatedObjectId = objectRepository.add(object, id);
            if (generatedObjectId == null)
                return null;

            temporaryIdMap.put(object.temporaryId, generatedObjectId);
        }

        for (var morphism : update.morphisms) {
            if (morphism.domId == null)
                morphism.domId = temporaryIdMap.get(morphism.temporaryDomId);

            if (morphism.codId == null)
                morphism.codId = temporaryIdMap.get(morphism.temporaryCodId);

            var morphismResult = morphismRepository.add(morphism, id);

            if (morphismResult == null)
                return null;
        }

        return find(id);
    }

    /*
    public Integer add(SchemaCategory schema)
    {
        return repository.add(schema);
    }
    */
}
