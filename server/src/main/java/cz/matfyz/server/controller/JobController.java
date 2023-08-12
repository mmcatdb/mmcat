package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.JobDetail;
import cz.matfyz.server.entity.job.JobInit;
import cz.matfyz.server.entity.job.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.job.payload.JsonLdToCategoryPayload;
import cz.matfyz.server.entity.job.payload.ModelToCategoryPayload;
import cz.matfyz.server.service.DataSourceService;
import cz.matfyz.server.service.JobService;
import cz.matfyz.server.service.LogicalModelService;
import cz.matfyz.server.utils.UserStore;

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

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping("/schema-categories/{categoryId}/jobs")
    public List<JobDetail> getAllJobsInCategory(@PathVariable Id categoryId) {
        final var jobs = service.findAllInCategory(categoryId);

        return jobs.stream().map(this::jobToJobDetail).toList();
    }

    @GetMapping("/jobs/{id}")
    public JobDetail getJob(@PathVariable Id id) {
        return jobToJobDetail(service.find(id));
    }

    @PostMapping("/jobs")
    public JobDetail createNewJob(@RequestBody JobInit jobInit) {
        return jobToJobDetail(service.createNew(new Job.Builder().fromInit(jobInit)));
    }

    @PostMapping("/jobs/{id}/start")
    public JobDetail startJob(@PathVariable Id id, HttpSession session) {
        final var job = service.find(id);
    
        final var store = UserStore.fromSession(session);
        final var startedJob = service.start(job, store);
        if (startedJob == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Job " + id + " could not be started.");

        return jobToJobDetail(startedJob);
    }

    @DeleteMapping("/jobs/{id}")
    public void deleteJob(@PathVariable Id id) {
        boolean result = service.delete(id);
        if (!result)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/jobs/{id}/cancel")
    public JobDetail cancelJob(@PathVariable Id id, HttpSession session) {
        final var job = service.find(id);

        final var canceledJob = service.cancel(job);
        if (canceledJob == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Job " + id + " could not be canceled.");

        return jobToJobDetail(canceledJob);
    }

    private JobDetail jobToJobDetail(Job job) {        
        if (job.payload instanceof ModelToCategoryPayload modelToCategoryPayload) {
            final var logicalModel = logicalModelService.find(modelToCategoryPayload.logicalModelId()).toInfo();
            return new JobDetail(job, new ModelToCategoryPayload.Detail(logicalModel));
        }
        else if (job.payload instanceof CategoryToModelPayload categoryToModelPayload) {
            final var logicalModel = logicalModelService.find(categoryToModelPayload.logicalModelId()).toInfo();
            return new JobDetail(job, new CategoryToModelPayload.Detail(logicalModel));
        }
        else if (job.payload instanceof JsonLdToCategoryPayload jsonLdToCategoryPayload) {
            final var logicalModel = dataSourceService.find(jsonLdToCategoryPayload.dataSourceId());
            return new JobDetail(job, new JsonLdToCategoryPayload.Detail(logicalModel));
        }

        throw new UnsupportedOperationException();
    }

}
