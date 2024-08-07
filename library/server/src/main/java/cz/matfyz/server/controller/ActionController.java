package cz.matfyz.server.controller;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.controller.LogicalModelController.LogicalModelInfo;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.ActionPayload;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.datasource.DatasourceDetail;

import cz.matfyz.server.service.ActionService;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActionController {

    @Autowired
    private ActionService service;

    @Autowired
    private LogicalModelRepository logicalModelRepository;

    @Autowired
    private LogicalModelController logicalModelController;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private DatasourceController datasourceController;

    @GetMapping("/schema-categories/{categoryId}/actions")
    public List<ActionDetail> getAllActionsInCategory(@PathVariable Id categoryId) {
        final var actions = service.findAllInCategory(categoryId);

        return actions.stream().map(this::actionToDetail).toList();
    }

    @GetMapping("/actions/{id}")
    public ActionDetail getAction(@PathVariable Id id) {
        return actionToDetail(service.find(id));
    }

    public record ActionInit(
        Id categoryId,
        String label,
        ActionPayload payload
    ) {}

    @PostMapping("/actions")
    public ActionDetail createAction(@RequestBody ActionInit actionInit) {
        return actionToDetail(service.create(actionInit));
    }

    @DeleteMapping("/actions/{id}")
    public void deleteAction(@PathVariable Id id) {
        service.delete(id);
    }

    private ActionDetail actionToDetail(Action action) {
        return new ActionDetail(action, actionPayloadToDetail(action.payload));
    }

    // TODO extremely unefficient - load all models and data sources at once.
    // TODO switch to pattern matching when available.
    ActionPayloadDetail actionPayloadToDetail(ActionPayload payload) {
        if (payload instanceof ModelToCategoryPayload modelToCategoryPayload) {
            final var model = logicalModelRepository.find(modelToCategoryPayload.logicalModelId());
            final var info = logicalModelController.createInfo(model);
            return new ModelToCategoryPayloadDetail(info);
        }
        if (payload instanceof CategoryToModelPayload categoryToModelPayload) {
            final var model = logicalModelRepository.find(categoryToModelPayload.logicalModelId());
            final var info = logicalModelController.createInfo(model);
            return new CategoryToModelPayloadDetail(info);
        }
        if (payload instanceof UpdateSchemaPayload updateSchemaPayload) {
            return new UpdateSchemaPayloadDetail(updateSchemaPayload.prevVersion(), updateSchemaPayload.nextVersion());
        }
        if (payload instanceof RSDToCategoryPayload rsdToCategoryPayload) {
            final var datasource = datasourceRepository.find(rsdToCategoryPayload.datasourceId());
            final var datasourceDetail = datasourceController.datasourceToDetail(datasource);
            final var kindName = rsdToCategoryPayload.kindName();
            return new RSDToCategoryPayloadDetail(datasourceDetail, kindName);
        }

        throw new UnsupportedOperationException("Unsupported action type: " + payload.getClass().getSimpleName() + ".");
    }

    record ActionDetail(
        Id id,
        Id categoryId,
        String label,
        ActionPayloadDetail payload
    ) {
        ActionDetail(Action action, ActionPayloadDetail payload) {
            this(action.id(), action.categoryId, action.label, payload);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CategoryToModelPayloadDetail.class, name = "CategoryToModel"),
        @JsonSubTypes.Type(value = ModelToCategoryPayloadDetail.class, name = "ModelToCategory"),
        @JsonSubTypes.Type(value = UpdateSchemaPayloadDetail.class, name = "UpdateSchema"),
        @JsonSubTypes.Type(value = RSDToCategoryPayloadDetail.class, name = "RSDToCategory"),
    })
    interface ActionPayloadDetail {}

    record CategoryToModelPayloadDetail(
        LogicalModelInfo logicalModel
    ) implements ActionPayloadDetail {}

    record ModelToCategoryPayloadDetail(
        LogicalModelInfo logicalModel
    ) implements ActionPayloadDetail {}

    record UpdateSchemaPayloadDetail(
        Version prevVersion,
        Version nextVersion
    ) implements ActionPayloadDetail {}

    record RSDToCategoryPayloadDetail(
        DatasourceDetail datasource,
        String kindName
    ) implements ActionPayloadDetail {}

}
