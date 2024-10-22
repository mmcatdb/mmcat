package cz.matfyz.server.service;

import cz.matfyz.server.entity.LogicalModel;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.entity.evolution.MappingEvolution;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.EvolutionRepository;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MappingService {

    @Autowired
    private MappingRepository repository;

    @Autowired
    private SchemaCategoryRepository categoryRepository;

    @Autowired
    private LogicalModelRepository logicalModelRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private EvolutionRepository evolutionRepository;

    @Autowired
    private QueryRepository queryRepository;

    public MappingWrapper create(MappingInit init) {
        final var category = categoryRepository.find(init.categoryId());
        LogicalModelWithDatasource logicalModel;
        try {
            logicalModel = logicalModelRepository.find(init.categoryId(), init.datasourceId());
        }
        catch (Exception e) {
            // TODO This is just atrocious. It makes me feel sick. Will be fixed at the exact moment we send the logical models to the Jesus.
            final var datasource = datasourceRepository.find(init.datasourceId());
            logicalModel = logicalModelService.create(init.categoryId(), init.datasourceId(), datasource.label);
        }

        final var newVersion = category.systemVersion().generateNext();
        final var mapping = MappingWrapper.createNew(newVersion, logicalModel.logicalModel().id(), init.rootObjectKey(), init.primaryKey(), init.kindName(), init.accessPath());
        // FIXME Add some data to the evolution.
        final var evolution = MappingEvolution.createNew(category.id(), newVersion, mapping.id(), null);

        repository.save(mapping);
        evolutionRepository.create(evolution);

        propagateEvolution(category, evolution);

        return mapping;
    }

    // FIXME Define mapping edit ...
    public void update(MappingWrapper mapping, Object edit) {
        final var logicalModel = logicalModelRepository.find(mapping.logicalModelId);
        final var category = categoryRepository.find(logicalModel.logicalModel().categoryId);

        final var newVersion = category.systemVersion().generateNext();
        final var evolution = MappingEvolution.createNew(category.id(), newVersion, mapping.id(), edit);

        mapping.updateVersion(newVersion, category.systemVersion());
        // FIXME Update the mapping.

        repository.save(mapping);
        evolutionRepository.create(evolution);

        propagateEvolution(category, evolution);
    }

    // TODO Probably should be moved to dedicated service once we have orm.
    private void propagateEvolution(SchemaCategoryWrapper category, MappingEvolution evolution) {
        final var oldVersion = category.systemVersion;

        category.systemVersion = evolution.version;
        category.updateLastValid(evolution.version);
        categoryRepository.save(category);

        // All other mappings are independed on this mapping so we can propagate the evolution.
        // TODO make more efficient with orm.
        repository.findAllInCategory(category.id()).stream()
            .filter(mapping -> mapping.lastValid().equals(oldVersion))
            .forEach(mapping -> {
                mapping.updateLastValid(evolution.version);
                repository.save(mapping);
            });

        // The same holds true for queries.
        queryRepository.findAllInCategory(category.id(), null).stream()
            .filter(query -> query.lastValid().equals(oldVersion))
            .forEach(query -> {
                query.updateLastValid(evolution.version);
                queryRepository.save(query);
            });
    }

}
