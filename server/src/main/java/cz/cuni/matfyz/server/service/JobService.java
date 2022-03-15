package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.DatabaseRepository;
import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.repository.MappingRepository;
import cz.cuni.matfyz.server.repository.SchemaCategoryRepository;
import cz.cuni.matfyz.server.repository.SchemaObjectRepository;
import cz.cuni.matfyz.transformations.ModelToCategory;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.server.entity.Database;
import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.entity.JobData;
import cz.cuni.matfyz.server.entity.MappingWrapper;
import cz.cuni.matfyz.server.entity.SchemaCategoryWrapper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;


/**
 * 
 * @author jachym.bartik
 */
@Service
public class JobService
{
    @Autowired
    private JobRepository repository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private DatabaseRepository databaseRepository;

    @Autowired
    private SchemaObjectRepository schemaObjectRepository;

    @Autowired
    private SchemaCategoryService schemaCategoryService;

    public List<Job> findAll()
    {
        return repository.findAll();
    }

    public Job find(int id)
    {
        return repository.find(id);
    }

    public Job createNew(int mappingId, String jsonValue)
    {
        var jobData = new JobData(mappingId, jsonValue);
        Integer generatedId = repository.add(jobData);

        return generatedId == null ? null : new Job(generatedId, jobData);
    }

    public boolean execute(Job job)
    {
        //if (job.type != "modelToCategory") // TODO
        //    return false

        try {
            return modelToCategoryAlgorithm(job);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    private boolean modelToCategoryAlgorithm(Job job) throws Exception
    {
        // službě se předají informace z vyplněného formuláře
        // klasicky post pošle data, bude tu víc věcí než jenom job
            // najít jednoduchý spring formulář (především POST)

        /*
        - session fungující na principu key-value databáze
            - tu si mám pamatovat instanční kategorii
            - uživatel se může rozhodnut, že jí chce materializovat a uložit přes jiné mapování do nějaké další databáze
            - resp. očekává se, že to tak udělá 
        - druhý případ - uživatel nakliká několik kroků najednou, uloží se výsledek (opět do session) a zbytek se zahodí
            - nemusíme uvažovat, že by instanční kategorie byla příliš velká, takže lze předpokládat, že se to do session vejde
            - je to dáno tím, že implementujeme jenom prototyp (proof of concept) - to je nutné napsat i do diplomky
                - podívat se na reddis a jak pracuje se sdílenou pamětí (nebo jiné in-memory datazábe)
        - Martin Svoboda má roota na nosql server - zeptat se jeho, jestli stačí skript nebo návod
            - zmínit od koho mám kontakt, přidat do kopie
        */

        /*
            faster xml / json (jackson)
        */

        
        MappingWrapper mappingWrapper = mappingRepository.find(job.mappingId);
        Database database = databaseRepository.find(mappingWrapper.databaseId);
        var pullWrapper = database.getPullWraper();
        
        var jsonObject = new JSONObject(mappingWrapper.jsonValue);
        Mapping mapping = new Mapping.Builder().fromJSON(jsonObject);

        var rootObjectWrapper = schemaObjectRepository.find(mappingWrapper.rootObjectId);
        SchemaCategoryWrapper schemaWrapper = schemaCategoryService.findWrapper(mappingWrapper.schemaId);

        var category = schemaWrapper.toSchemaCategory();
        var rootObjectKey = rootObjectWrapper.toSchemaObject().key();
        mapping.setRootObject(category.keyToObject(rootObjectKey));
        var forest = pullWrapper.pullForest(mapping.accessPath(), new PullWrapperOptions.Builder().buildWithKindName(jsonObject.getString("kindName")));

        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(category).build();
        
        var transformation = new ModelToCategory();
		transformation.input(category, instance, forest, mapping);
		transformation.algorithm();

        System.out.println(instance);

        return true;
    }
}
