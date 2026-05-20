package cz.matfyz.server.job;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.datasource.DatasourceResponse;
import cz.matfyz.server.evolution.SchemaEvolutionPayload;
import cz.matfyz.server.inference.InferencePayload;
import cz.matfyz.server.instance.CategoryToModelPayload;
import cz.matfyz.server.instance.ModelToCategoryPayload;
import cz.matfyz.server.mapping.MappingRepository;
import cz.matfyz.server.mapping.MappingController.MappingInfo;
import cz.matfyz.server.utils.entity.Id;

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
    private ActionRepository repository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private MappingRepository mappingRepository;

    private record ActionInfo(
        Id id,
        Id categoryId,
        String label
    ) {
        ActionInfo(Action action) {
            this(action.id(), action.categoryId, action.label);
        }
    }

    @GetMapping("/schema-categories/{categoryId}/actions")
    public List<ActionInfo> getAllActionsInCategory(@PathVariable Id categoryId) {
        return repository.findAllInCategory(categoryId).stream().map(ActionInfo::new).toList();
    }

    @GetMapping("/actions/{id}")
    public ActionDetail getAction(@PathVariable Id id) {
        return actionToDetail(repository.find(id));
    }

    private record ActionInit(
        Id categoryId,
        String label,
        List<JobPayload> payloads
    ) {}

    @PostMapping("/actions")
    public ActionDetail createAction(@RequestBody ActionInit init) {
        final var action = service.create(init.categoryId(), init.label(), init.payloads());
        return actionToDetail(action);
    }

    @DeleteMapping("/actions/{id}")
    public void deleteAction(@PathVariable Id id) {
        repository.delete(id);
    }

    private ActionDetail actionToDetail(Action action) {
        final var payloads = action.payloads.stream().map(p -> jobPayloadToDetail(p, action.categoryId)).toList();
        return new ActionDetail(action, payloads);
    }

    // TODO extremely unefficient - load all models and datasources at once.
    JobPayloadDetail jobPayloadToDetail(JobPayload payload, Id categoryId) {
        return switch (payload) {
            case ModelToCategoryPayload p -> {
                final var datasourceEntity = datasourceRepository.find(p.datasourceId());
                final var datasource = DatasourceResponse.fromEntity(datasourceEntity);
                if (p.mappingIds().isEmpty())
                    yield new ModelToCategoryPayloadDetail(datasource, List.of());

                final var mappingInfos = mappingRepository.findAllInCategory(categoryId, datasourceEntity.id()).stream()
                    .filter(entity -> p.mappingIds().contains(entity.id()))
                    .map(MappingInfo::fromEntity)
                    .toList();

                yield new ModelToCategoryPayloadDetail(datasource, mappingInfos);
            }
            case CategoryToModelPayload p -> {
                final var datasourceEntity = datasourceRepository.find(p.datasourceId());
                final var datasource = DatasourceResponse.fromEntity(datasourceEntity);
                if (p.mappingIds().isEmpty())
                    yield new CategoryToModelPayloadDetail(datasource, List.of());

                final var mappingInfos = mappingRepository.findAllInCategory(categoryId, datasourceEntity.id()).stream()
                    .filter(entity -> p.mappingIds().contains(entity.id()))
                    .map(MappingInfo::fromEntity)
                    .toList();

                yield new CategoryToModelPayloadDetail(datasource, mappingInfos);
            }
            case SchemaEvolutionPayload p -> new SchemaEvolutionPayloadDetail(p.prevVersion(), p.nextVersion());
            case InferencePayload p -> {
                final var datasources = p.datasourceIds().stream()
                    .map(datasourceRepository::find)
                    .map(DatasourceResponse::fromEntity)
                    .toList();
                yield new InferencePayloadDetail(datasources);
            }
            default -> throw new UnsupportedOperationException("Unsupported action type: " + payload.getClass().getSimpleName() + ".");
        };
    }

    record ActionDetail(
        Id id,
        Id categoryId,
        String label,
        List<JobPayloadDetail> payloads
    ) {
        ActionDetail(Action action, List<JobPayloadDetail> payloads) {
            this(action.id(), action.categoryId, action.label, payloads);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CategoryToModelPayloadDetail.class, name = "CategoryToModel"),
        @JsonSubTypes.Type(value = ModelToCategoryPayloadDetail.class, name = "ModelToCategory"),
        @JsonSubTypes.Type(value = SchemaEvolutionPayloadDetail.class, name = "SchemaEvolution"),
        @JsonSubTypes.Type(value = InferencePayloadDetail.class, name = "Inference"),
    })
    interface JobPayloadDetail {}

    record CategoryToModelPayloadDetail(
        DatasourceResponse datasource,
        List<MappingInfo> mappings
    ) implements JobPayloadDetail {}

    record ModelToCategoryPayloadDetail(
        DatasourceResponse datasource,
        List<MappingInfo> mappings
    ) implements JobPayloadDetail {}

    record SchemaEvolutionPayloadDetail(
        Version prevVersion,
        Version nextVersion
    ) implements JobPayloadDetail {}

    record InferencePayloadDetail(
        List<DatasourceResponse> datasources
    ) implements JobPayloadDetail {}

}
