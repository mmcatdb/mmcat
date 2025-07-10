package cz.matfyz.server.service;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.category.SMO;
import cz.matfyz.evolution.category.SchemaEvolutionAlgorithm;
import cz.matfyz.evolution.exception.VersionException;
import cz.matfyz.evolution.metadata.MMO;
import cz.matfyz.evolution.metadata.MetadataEvolutionAlgorithm;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.evolution.SchemaEvolution;
import cz.matfyz.server.global.RequestContext;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.server.repository.EvolutionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemaCategoryService {

    @Autowired
    private RequestContext request;

    @Autowired
    private SchemaCategoryRepository repository;

    @Autowired
    private EvolutionRepository evolutionRepository;

    @Autowired
    private JobService jobService;

    public SchemaCategoryEntity create(String label) {
        final String finalLabel = findUniqueLabel(label);

        final var schema = new SchemaCategory();
        final var metadata = MetadataCategory.createEmpty(schema);
        final var entity = SchemaCategoryEntity.createNew(finalLabel, schema, metadata);

        repository.save(entity);
        final var session = jobService.createSession(entity.id());
        request.setup(session.id());

        return entity;
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

    public SchemaCategoryEntity update(Id id, SchemaEvolutionInit evolutionInit) {
        final SchemaCategoryEntity categoryEntity = repository.find(id);
        if (!evolutionInit.prevVersion().equals(categoryEntity.version()))
            throw VersionException.mismatch(evolutionInit.prevVersion(), categoryEntity.version());

        final var newVersion = categoryEntity.systemVersion.generateNext();
        final var evolution = SchemaEvolution.createFromInit(id, newVersion, evolutionInit);

        final SchemaEvolutionAlgorithm schemaAlgorithm = evolution.toSchemaAlgorithm(evolutionInit.prevVersion());
        final SchemaCategory schema = categoryEntity.toSchemaCategory();
        final MetadataEvolutionAlgorithm metadataAlgorithm = evolution.toMetadataAlgorithm(evolutionInit.prevVersion());
        final MetadataCategory metadata = categoryEntity.toMetadataCategory(schema);
        schemaAlgorithm.up(schema, metadata);

        metadataAlgorithm.up(metadata);

        categoryEntity.updateVersion(newVersion, categoryEntity.systemVersion);
        categoryEntity.systemVersion = newVersion;
        categoryEntity.schema = SchemaSerializer.serialize(schema);
        categoryEntity.metadata = MetadataSerializer.serialize(metadata);

        repository.save(categoryEntity);
        evolutionRepository.create(evolution);

        jobService.createRun(
            id,
            "Update queries to v. " + evolution.version,
            List.of(new UpdateSchemaPayload(evolutionInit.prevVersion(), evolution.version))
        );

        return categoryEntity;
    }

}
