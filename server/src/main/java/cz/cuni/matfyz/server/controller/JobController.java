package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.service.JobService;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.server.view.NewJobView;
import cz.cuni.matfyz.server.entity.Job;

import java.util.*;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class JobController
{
    @Autowired
    private JobService service;

    @GetMapping("/jobs")
    public List<Job> getAllJobs()
    {
        return service.findAll();
    }

    @GetMapping("/jobs/{id}")
    public Job getJobById(@PathVariable Integer id)
    {
        Job job = service.find(id);
        if (job != null)
            return job;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/jobs")
    public Job createNewJob(@RequestBody NewJobView jobView)
    {
        Job newJob = service.createNew(new Job.Builder().fromArguments(null, jobView.mappingId, Job.Status.Ready));
        if (newJob != null)
            return newJob;
        
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    // jsf - funkce, které zjistí kód chyby a na to reagují

    @PostMapping("/jobs/{id}/start")
    public Job startJobById(@PathVariable int id, HttpSession session)
    {
        Job job = service.find(id);
        if (job == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not foud ... TODO"); // TODO

        var store = UserStore.fromSession(session);
        return service.start(job, store);
    }
}
