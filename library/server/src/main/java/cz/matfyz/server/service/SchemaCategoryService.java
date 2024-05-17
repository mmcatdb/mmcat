package cz.matfyz.server.service;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.server.builder.MetadataContext;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.MetadataUpdate;
import cz.matfyz.server.repository.SchemaCategoryRepository;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
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

    public SchemaCategoryInfo createNewInfo(SchemaCategoryInit init) {
        if (init.label() == null)
            return null;

        final var newWrapper = SchemaCategoryWrapper.createNew(init.label());
        final Id generatedId = repository.add(newWrapper);

        if (generatedId == null)
            return null;

        jobService.createSession(generatedId);

        return new SchemaCategoryInfo(generatedId, init.label(), newWrapper.version);
    }

    /*
     * Created for the case when I receive SchemaCategoryWrapper from mminfer
     * Be aware of the label in creating SchemaCategoryInit
     * Maybe now, I will have to accept SK not SKWrapper! and make the contentext here!
     * */
    public SchemaCategoryInfo createNewInfo(SchemaCategoryWrapper wrapper) {
        final Id generatedId = repository.add(wrapper);
        //System.out.println("This is the generated ID: " + generatedId);

        return generatedId == null ? null : new SchemaCategoryInfo(generatedId, "new_label", wrapper.version);
    }

    /***
     * Temporary workaround method for inference
     * Overwrites the existing empty SchemaCategory
     */
    public SchemaCategoryInfo overwriteInfo(SchemaCategoryWrapper wrapper, Id id) {
        final boolean saved = repository.save(wrapper, id);

        return saved ? new SchemaCategoryInfo(id, "new_label", wrapper.version) : null;
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return repository.findInfo(new Id("" + id));
    }

    public SchemaCategoryWrapper find(Id id) {
        return repository.find(id);
    }


    public @Nullable SchemaCategoryWrapper update(Id id, SchemaUpdateInit updateInit) {
        final SchemaCategoryWrapper wrapper = repository.find(id);
        final var update = SchemaUpdate.fromInit(updateInit, id);

        if (!update.prevVersion.equals(wrapper.version))
            return null;

        final MetadataContext context = new MetadataContext();
        final SchemaCategoryUpdate evolutionUpdate = update.toEvolution();
        final SchemaCategory category = wrapper.toSchemaCategory(context);
        evolutionUpdate.up(category);

        // The metadata is not versioned.
        // However, without it, the schema category can't be restored to it's previous version.
        // So, we might need to keep all metadata somewhere. Maybe even version it ...
        updateInit.metadata().forEach(m -> context.setPosition(m.key(), m.position()));
        context.setVersion(update.nextVersion);

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(category, context);

        if (!repository.update(newWrapper, update))
            return null;

        jobService.createSystemRun(
            id,
            "Update queries to v. " + update.nextVersion,
            new UpdateSchemaPayload(update.prevVersion, update.nextVersion)
        );

        return newWrapper;
    }

    public boolean updateMetadata(Id id, List<MetadataUpdate> metadataUpdates) {
        final var wrapper = repository.find(id);

        final var context = new MetadataContext();
        final var category = wrapper.toSchemaCategory(context);
        metadataUpdates.forEach(m -> context.setPosition(m.key(), m.position()));

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(category, context);

        return repository.updateMetadata(newWrapper);
    }

    public List<SchemaUpdate> findAllUpdates(Id id) {
        return repository.findAllUpdates(id);
    }

}
