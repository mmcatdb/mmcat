package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.DatabaseRepository;
import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.repository.MappingRepository;
import cz.cuni.matfyz.server.repository.SchemaCategoryRepository;
import cz.cuni.matfyz.server.repository.SchemaObjectRepository;
import cz.cuni.matfyz.transformations.algorithms.ModelToCategory;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.Result;
import cz.cuni.matfyz.server.builder.SchemaBuilder;
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
    private MappingService mappingService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SchemaCategoryService categoryService;

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

        var result = modelToCategoryAlgorithm(job);

        return result.status; // TODO

        //return false;
    }

    private Result<InstanceCategory> modelToCategoryAlgorithm(Job job)
    {       
        var mappingWrapper = mappingService.find(job.mappingId);
        var categoryWrapper = categoryService.find(mappingWrapper.categoryId);

        var mapping = new SchemaBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();

        AbstractPullWrapper pullWrapper = databaseService.find(mappingWrapper.databaseId)
            .getPullWraper();

        var process = new DatabaseToInstance();
        process.input(pullWrapper, mapping);
        
        return process.run();
    }
}
