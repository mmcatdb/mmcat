package cz.cuni.matfyz.server;

import java.util.ArrayList;

import java.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class JobController
{
    @GetMapping("/jobs")
    public List<Job> getAllJobs()
    {
        return new ArrayList<>(); // TODO
    }

    @GetMapping("/jobs/{id}")
    public Job getJobById(@PathVariable String id)
    {
        return null; // TODO
    }
}
