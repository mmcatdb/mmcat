package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.workflow.InferenceWorkflowData;
import cz.matfyz.server.entity.workflow.Workflow;
import cz.matfyz.server.repository.WorkflowRepository;
import cz.matfyz.server.service.SchemaCategoryService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowController {

    @Autowired
    private WorkflowRepository repository;

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

    private enum WorkflowType {
        inference,
    }

    private record WorkflowInit(
        String label,
        WorkflowType type
    ) {}

    @PostMapping("/workflows")
    public Workflow createWorkflow(@RequestBody WorkflowInit init) {
        final var category = schemaService.create(init.label);

        switch (init.type) {
            case inference -> {
                final var workflow = Workflow.createNew(category.id(), init.label, InferenceWorkflowData.createNew());
                repository.save(workflow);
                return workflow;
            }
        }

        throw new IllegalArgumentException("Unknown workflow type: " + init.type);
    }

}
