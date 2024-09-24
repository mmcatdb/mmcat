package cz.matfyz.server.service;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.exception.VersionException;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.repository.SchemaCategoryRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaCategoryService {

    @Autowired
    private SchemaCategoryRepository repository;

    @Autowired
    private JobService jobService;

    public List<SchemaCategoryInfo> findAllInfos() {
        return repository.findAllInfos();
    }

    public SchemaCategoryWrapper create(SchemaCategoryInit init) {
        final var schema = new SchemaCategory();
        final var metadata = MetadataCategory.createEmpty(schema);
        final var version = Version.generateInitial("0");
        final var wrapper = SchemaCategoryWrapper.fromSchemaCategory(null, init.label(), version, version, schema, metadata);
        repository.add(wrapper);
        jobService.createSession(wrapper.id());

        return wrapper;
    }

    public void replace(SchemaCategoryWrapper wrapper) {
        repository.save(wrapper);
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return repository.findInfo(new Id("" + id));
    }

    public SchemaCategoryWrapper find(Id id) {
        return repository.find(id);
    }

    public SchemaCategoryWrapper update(Id id, SchemaUpdateInit updateInit) {
        final SchemaCategoryWrapper wrapper = repository.find(id);
        final var update = SchemaUpdate.fromInit(updateInit, id, wrapper.systemVersion);

        if (!update.prevVersion.equals(wrapper.version))
            throw VersionException.mismatch(update.prevVersion, wrapper.version);

        final SchemaCategoryUpdate evolutionUpdate = update.toEvolution();
        final SchemaCategory schema = wrapper.toSchemaCategory();
        evolutionUpdate.up(schema);

        // The metadata is not versioned.
        // However, without it, the schema category can't be restored to it's previous version.
        // So, we might need to keep all metadata somewhere. Maybe even version it ...

        final var metadata = wrapper.toMetadataCategory(schema);
        updateInit.metadata().forEach(metadataUpdate -> metadataUpdate.up(metadata));

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(wrapper.id(), wrapper.label, update.nextVersion, update.nextVersion, schema, metadata);

        repository.update(newWrapper, update);

        jobService.createSystemRun(
            id,
            "Update queries to v. " + update.nextVersion,
            new UpdateSchemaPayload(update.prevVersion, update.nextVersion)
        );

        return newWrapper;
    }

    public List<SchemaUpdate> findAllUpdates(Id id) {
        return repository.findAllUpdates(id);
    }

}
