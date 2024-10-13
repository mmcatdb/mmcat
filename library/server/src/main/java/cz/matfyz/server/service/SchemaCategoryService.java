package cz.matfyz.server.service;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.exception.VersionException;
import cz.matfyz.evolution.metadata.MMO;
import cz.matfyz.evolution.metadata.MetadataEvolutionAlgorithm;
import cz.matfyz.evolution.schema.SMO;
import cz.matfyz.evolution.schema.SchemaEvolutionAlgorithm;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.evolution.SchemaEvolution;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.server.repository.EvolutionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemaCategoryService {

    @Autowired
    private SchemaCategoryRepository repository;

    @Autowired
    private EvolutionRepository evolutionRepository;

    @Autowired
    private JobService jobService;

    public SchemaCategoryWrapper create(String label) {
        final String finalLabel = findUniqueLabel(label);

        final var schema = new SchemaCategory();
        final var metadata = MetadataCategory.createEmpty(schema);
        final var wrapper = SchemaCategoryWrapper.createNew(finalLabel, schema, metadata);

        repository.save(wrapper);
        jobService.createSession(wrapper.id());

        return wrapper;
    }

    private String findUniqueLabel(String label) {
        final var used = repository.findAllInfos().stream().map(info -> info.label()).collect(Collectors.toSet());

        if (!used.contains(label))
            return label;

        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            final var newLabel = label + " (" + i + ")";
            if (!used.contains(newLabel))
                return newLabel;
        }

        throw new IllegalStateException("No unique label found");
    }

    public record SchemaEvolutionInit(
        Version prevVersion,
        List<SMO> schema,
        List<MMO> metadata
    ) {}

    public SchemaCategoryWrapper update(Id id, SchemaEvolutionInit evolutionInit) {
        final SchemaCategoryWrapper wrapper = repository.find(id);
        if (!evolutionInit.prevVersion().equals(wrapper.version()))
            throw VersionException.mismatch(evolutionInit.prevVersion(), wrapper.version());

        final var newVersion = wrapper.systemVersion.generateNext();
        final var evolution = SchemaEvolution.createFromInit(id, newVersion, evolutionInit);

        final SchemaEvolutionAlgorithm schemaAlgorithm = evolution.toSchemaAlgorithm(evolutionInit.prevVersion());
        final SchemaCategory schema = wrapper.toSchemaCategory();
        schemaAlgorithm.up(schema);

        final MetadataEvolutionAlgorithm metadataAlgorithm = evolution.toMetadataAlgorithm(evolutionInit.prevVersion());
        final MetadataCategory metadata = wrapper.toMetadataCategory(schema);
        metadataAlgorithm.up(metadata);

        wrapper.updateVersion(newVersion, wrapper.systemVersion);
        wrapper.systemVersion = newVersion;
        wrapper.schema = SchemaSerializer.serialize(schema);
        wrapper.metadata = MetadataSerializer.serialize(metadata);

        repository.save(wrapper);
        evolutionRepository.create(evolution);

        jobService.createSystemRun(
            id,
            "Update queries to v. " + evolution.version,
            new UpdateSchemaPayload(evolutionInit.prevVersion(), evolution.version)
        );

        return wrapper;
    }

}
