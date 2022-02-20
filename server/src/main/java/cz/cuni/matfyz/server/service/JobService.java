package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.entity.Job;

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

    public List<Job> findAll()
    {
        return repository.findAll();
    }
}
