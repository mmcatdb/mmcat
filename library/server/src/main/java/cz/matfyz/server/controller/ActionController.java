package cz.matfyz.server.controller;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.controller.LogicalModelController.LogicalModelInfo;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.DataSourceRepository;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.DatabaseRepository;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.ActionPayload;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.datasource.DataSource;

import cz.matfyz.server.service.ActionService;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
public class ActionController {

    @Autowired
    private ActionService service;

    @Autowired
    private LogicalModelRepository logicalModelRepository;

    @Autowired
    private DataSourceRepository dataSourceRepository;
    
    @Autowired
    private DatabaseRepository databaseRepository;

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
        boolean result = service.delete(id);
        if (!result)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private ActionDetail actionToDetail(Action action) {
        return new ActionDetail(action, actionPayloadToDetail(action.payload));
    }

    // TODO extremely unefficient - load all models and data sources at once.
    // TODO switch to pattern matching when available.
    ActionPayloadDetail actionPayloadToDetail(ActionPayload payload) {
        if (payload instanceof ModelToCategoryPayload modelToCategoryPayload) {
            final var model = logicalModelRepository.find(modelToCategoryPayload.logicalModelId());
            final var info = LogicalModelInfo.fromEntities(model);
            return new ModelToCategoryPayloadDetail(info);
        }
        if (payload instanceof CategoryToModelPayload categoryToModelPayload) {
            final var model = logicalModelRepository.find(categoryToModelPayload.logicalModelId());
            final var info = LogicalModelInfo.fromEntities(model);
            return new CategoryToModelPayloadDetail(info);
        }
        if (payload instanceof UpdateSchemaPayload updateSchemaPayload) {
            return new UpdateSchemaPayloadDetail(updateSchemaPayload.prevVersion(), updateSchemaPayload.nextVersion());
        }
        if (payload instanceof RSDToCategoryPayload rsdToCategoryPayload) {
            DataSource dataSource = null;
            DatabaseEntity database = null;
            if (rsdToCategoryPayload.dataSourceId() != null ){
                dataSource = dataSourceRepository.find(rsdToCategoryPayload.dataSourceId());
            }
            else {
                database = databaseRepository.find(rsdToCategoryPayload.databaseId());
            }
            
            return new RSDToCategoryPayloadDetail(dataSource, database);
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
            this(action.id, action.categoryId, action.label, payload);
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
            DataSource dataSource,
            DatabaseEntity database
    ) implements ActionPayloadDetail {}

}
