package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.job.Job;
import cz.cuni.matfyz.server.entity.job.JobInit;
import cz.cuni.matfyz.server.service.JobService;
import cz.cuni.matfyz.server.utils.UserStore;

import java.util.List;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class JobController {

    @Autowired
    private JobService service;

    @GetMapping("/schema-categories/{categoryId}/jobs")
    public List<Job> getAllJobsInCategory(@PathVariable int categoryId) {
        return service.findAllInCategory(categoryId);
    }

    @GetMapping("/jobs/{id}")
    public Job getJob(@PathVariable Integer id) {
        Job job = service.find(id);
        if (job != null)
            return job;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/jobs")
    public Job createNewJob(@RequestBody JobInit jobInit) {
        //Job newJob = service.createNew(new Job.Builder().fromArguments(null, jobInit.mappingId(), null, jobInit.name(), Job.Type.valueOf(jobInit.type()), Job.Status.Ready));
        Job newJob = service.createNew(new Job.Builder().fromArguments(null, jobInit.logicalModelId(), null, jobInit.label(), jobInit.type(), Job.Status.Ready));
        if (newJob != null)
            return newJob;
        
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/jobs/{id}/start")
    public Job startJob(@PathVariable int id, HttpSession session) {
        Job job = service.find(id);
        if (job == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not foud.");

        var store = UserStore.fromSession(session);
        return service.start(job, store);
    }

    @DeleteMapping("/jobs/{id}")
    public void deleteJob(@PathVariable Integer id) {
        boolean result = service.delete(id);
        if (!result)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
