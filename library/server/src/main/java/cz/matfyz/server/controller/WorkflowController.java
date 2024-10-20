package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.workflow.InferenceWorkflowData;
import cz.matfyz.server.entity.workflow.Workflow;
import cz.matfyz.server.entity.workflow.Workflow.WorkflowData;
import cz.matfyz.server.entity.workflow.Workflow.WorkflowType;
import cz.matfyz.server.repository.WorkflowRepository;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.server.service.WorkflowService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowController {

    @Autowired
    private WorkflowRepository repository;

    @Autowired
    private WorkflowService service;

    @Autowired
    private SchemaCategoryService schemaService;

    @GetMapping("/workflows")
    public List<Workflow> getAllWorkflowsInCategory() {
        return repository.findAll();
    }

    @GetMapping("/workflows/{id}")
    public Workflow getWorkflow(@PathVariable Id id) {
        return repository.find(id);
    }

    private record WorkflowInit(
        String label,
        WorkflowType type
    ) {}

    @PostMapping("/workflows")
    public Workflow createWorkflow(@RequestBody WorkflowInit init) {
        final var category = schemaService.create(init.label);
        final var data = WorkflowData.createNew(init.type);
        final var workflow = Workflow.createNew(category.id(), init.label, data);

        repository.save(workflow);

        return workflow;
    }

    @PutMapping("/workflows/{id}/data")
    public Workflow updateWorkflowData(@PathVariable Id id, @RequestBody WorkflowData data) {
        final var workflow = repository.find(id);
        workflow.data = data;
        repository.save(workflow);

        return workflow;
    }

    @PostMapping("/workflows/{id}/continue")
    public Workflow continueWorkflow(@PathVariable Id id) {
        final var workflow = repository.find(id);
        return service.continueWorkflow(workflow);
    }

}
