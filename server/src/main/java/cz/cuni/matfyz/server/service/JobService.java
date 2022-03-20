package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.entity.Job.Status;

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
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobRepository repository;

    @Autowired
    private AsyncJobService asyncService;

    public List<Job> findAll() {
        return repository.findAll();
    }

    public Job find(int id) {    
        return repository.find(id);
    }

    public Job createNew(Job job) {
        job.status = Status.Ready;
        Integer generatedId = repository.add(job);

        return generatedId == null ? null : new Job.Builder().fromArguments(generatedId, job.mappingId, job.status);
    }

    public Job start(Job job, UserStore store) {
        //if (job.type != "modelToCategory") // TODO
        //    return false
        LOGGER.info("START JOB");
        setJobStatus(job, Job.Status.Running);
        asyncService.runJob(job, store);
        LOGGER.info("START JOB END");
        return job;

        //
    }

    private void setJobStatus(Job job, Job.Status status) {
        job.status = status;
        repository.updateJSONValue(job);
    }

}
