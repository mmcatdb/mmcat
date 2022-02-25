package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.service.JobService;
import cz.cuni.matfyz.server.entity.Job;

import java.util.*;
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
    private JobService jobService;

    @GetMapping("/jobs")
    public List<Job> getAllJobs()
    {
        return jobService.findAll();
    }

    @GetMapping("/jobs/{id}")
    public Job getJobById(@PathVariable String id)
    {
        Job job = jobService.find(id);
        if (job != null)
            return job;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/jobs")
    public Job createNewJob(@RequestBody String accessPathAsString)
    {
        Job job = jobService.createNew(accessPathAsString);
        /*
        if (job != null)
            return job;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        */

        return job;
    }
}
