package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.utils.UserStore;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class JobService {

    @Autowired
    private JobRepository repository;

    @Autowired
    private AsyncJobService asyncService;

    public List<Job> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public Job find(Id id) {
        return repository.find(id);
    }

    public Job createNew(Job job) {
        Id generatedId = repository.add(job);

        return repository.find(generatedId);
    }

    public Job start(Job job, UserStore store) {
        if (!setJobState(job, Job.State.Running))
            return null;

        asyncService.runJob(job, store);

        return job;
    }

    private boolean setJobState(Job job, Job.State state) {
        job.state = state;
        return repository.updateJsonValue(job);
    }

    public boolean delete(Id id) {
        return repository.delete(id);
    }

    public Job cancel(Job job) {
        return setJobState(job, Job.State.Canceled) ? job : null;
    }

}
