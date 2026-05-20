package cz.matfyz.server.evolution;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.category.SchemaEvolutionAlgorithm;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.evolution.querying.QueryEvolutionResult;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryRepository;
import cz.matfyz.server.evolution.QueryEvolution;
import cz.matfyz.server.job.Job;
import cz.matfyz.server.job.Run;
import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryRepository;
import cz.matfyz.server.utils.entity.Entity;
import cz.matfyz.server.utils.entity.Id;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvolutionService {

    @Autowired
    private EvolutionRepository repository;

    @Autowired
    private SchemaCategoryRepository schemaRepository;

    @Autowired
    private QueryRepository queryRepository;

    public void startJob(Run run, Job job, SchemaEvolutionPayload payload) {
        // FIXME filter correctly by versions.
        final List<Query> prevQueries = queryRepository.findAllInCategory(run.categoryId, payload.prevVersion());
        final List<Query> nextQueries = queryRepository.findAllInCategory(run.categoryId, payload.nextVersion());

        // Some high order magic right here because Java generics suck ass!
        @SuppressWarnings("unchecked")
        final List<Query> filteredPrevQueries = (List<Query>) (Object) ArrayUtils.filterSorted((List<Entity>) (Object) prevQueries, (List<Entity>) (Object) nextQueries);

        final QueryEvolver evolver = createQueryEvolver(run.categoryId, payload.prevVersion(), payload.nextVersion());

        // FIXME doesn't do anything now.
        // for (final var query : filteredPrevQueries) {
        //     final QueryEvolutionResult updateResult = evolver.run(query.content);
        //     final var newVersion = QueryEvolution.createNew(
        //         run.categoryId,
        //         // FIXME get system version
        //         payload.nextVersion(),
        //         query.id(),
        //         updateResult.nextContent,
        //         updateResult.errors
        //     );
        //     repository.create(newVersion);
        // }
    }

    private QueryEvolver createQueryEvolver(Id categoryId, Version prevVersion, Version nextVersion) {
        final SchemaCategoryEntity categoryEntity = schemaRepository.find(categoryId);
        final List<SchemaEvolutionAlgorithm> updates = repository
            .findAllSchemaEvolutions(categoryId).stream()
            // TODO Check if the version comparison is correct (with respect to the previous algorithm)
            .filter(u -> u.version.compareTo(prevVersion) > 0 && u.version.compareTo(nextVersion) <= 0)
            // .filter(u -> u.prevVersion.compareTo(prevVersion) >= 0 && u.nextVersion.compareTo(nextVersion) <= 0)
            .map(u -> u.toSchemaAlgorithm(prevVersion)).toList();
            // .map(SchemaUpdate::toEvolution).toList();

        final SchemaCategory prevSchema = categoryEntity.toSchemaCategory();
        final MetadataCategory prevMetadata = categoryEntity.toMetadataCategory(prevSchema);
        final SchemaCategory nextSchema = categoryEntity.toSchemaCategory();
        final MetadataCategory nextMetadata = categoryEntity.toMetadataCategory(nextSchema);
        SchemaEvolutionAlgorithm.setToVersion(prevSchema, prevMetadata, updates, categoryEntity.version(), prevVersion);
        SchemaEvolutionAlgorithm.setToVersion(nextSchema, nextMetadata, updates, categoryEntity.version(), nextVersion);

        return new QueryEvolver(prevSchema, nextSchema, updates);
    }

}
