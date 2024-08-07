package cz.matfyz.server.service;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
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
import cz.matfyz.server.utils.LayoutUtils;

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

    public SchemaCategoryWrapper create(SchemaCategoryInit init) {
        final var category = new SchemaCategory(init.label());
        final var wrapper = newCategoryToWrapper(category);
        repository.add(wrapper);
        jobService.createSession(wrapper.id());

        return wrapper;
    }

    public void replace(SchemaCategoryWrapper wrapper) {
        repository.save(wrapper);
    }

    public SchemaCategoryWrapper newCategoryToWrapper(SchemaCategory category) {
        final var version = Version.generateInitial("0");
        final var layout = LayoutUtils.computeObjectsLayout(category);

        final var context = new MetadataContext()
            .setVersion(version)
            .setSystemVersion(version)
            .setPostitions(layout);

        return SchemaCategoryWrapper.fromSchemaCategory(category, context);
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return repository.findInfo(new Id("" + id));
    }

    public SchemaCategoryWrapper find(Id id) {
        return repository.find(id);
    }

    public @Nullable SchemaCategoryWrapper update(Id id, SchemaUpdateInit updateInit) {
        final SchemaCategoryWrapper wrapper = repository.find(id);
        final var update = SchemaUpdate.fromInit(updateInit, id, wrapper.systemVersion);

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
        context.setSystemVersion(update.nextVersion);

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(category, context);

        repository.update(newWrapper, update);

        jobService.createSystemRun(
            id,
            "Update queries to v. " + update.nextVersion,
            new UpdateSchemaPayload(update.prevVersion, update.nextVersion)
        );

        return newWrapper;
    }

    public void updateMetadata(Id id, List<MetadataUpdate> metadataUpdates) {
        final var wrapper = repository.find(id);

        final var context = new MetadataContext();
        final var category = wrapper.toSchemaCategory(context);
        metadataUpdates.forEach(m -> context.setPosition(m.key(), m.position()));

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(category, context);

        repository.updateMetadata(newWrapper);
    }

    public List<SchemaUpdate> findAllUpdates(Id id) {
        return repository.findAllUpdates(id);
    }

}
