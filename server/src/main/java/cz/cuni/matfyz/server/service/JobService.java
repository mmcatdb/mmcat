package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.server.entity.Job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 
 * @author jachym.bartik
 */
@Service
public class JobService {

    @Autowired
    private JobRepository repository;

    @Autowired
    private AsyncJobService asyncService;

    public List<Job> findAllInCategory(int categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public Job find(int id) {    
        return repository.find(id);
    }

    public Job createNew(Job job) {
        Integer generatedId = repository.add(job);

        return repository.find(generatedId);
    }

    public Job start(Job job, UserStore store) {
        setJobStatus(job, Job.Status.Running);
        asyncService.runJob(job, store);

        return job;
    }

    private void setJobStatus(Job job, Job.Status status) {
        job.status = status;
        repository.updateJSONValue(job);
    }

    public boolean delete(Integer id) {
        return repository.delete(id);
    }

}
