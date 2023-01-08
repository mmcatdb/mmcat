package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.job.Job;
import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.utils.UserStore;

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
        if (!job.isValid())
            return null;

        Id generatedId = repository.add(job);

        return repository.find(generatedId);
    }

    public Job start(Job job, UserStore store) {
        if (!setJobStatus(job, Job.Status.Running))
            return null;

        asyncService.runJob(job, store);

        return job;
    }

    private boolean setJobStatus(Job job, Job.Status status) {
        job.status = status;
        return repository.updateJSONValue(job);
    }

    public boolean delete(Id id) {
        return repository.delete(id);
    }

    public Job cancel(Job job) {
        return setJobStatus(job, Job.Status.Canceled) ? job : null;
    }

}
