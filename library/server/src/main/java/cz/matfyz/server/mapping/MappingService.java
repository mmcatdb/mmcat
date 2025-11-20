package cz.matfyz.server.mapping;

import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryRepository;
import cz.matfyz.server.evolution.EvolutionRepository;
import cz.matfyz.server.evolution.MappingEvolution;
import cz.matfyz.server.mapping.MappingController.MappingEdit;
import cz.matfyz.server.mapping.MappingController.MappingInit;
import cz.matfyz.server.querying.QueryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MappingService {

    @Autowired
    private MappingRepository repository;

    @Autowired
    private SchemaCategoryRepository categoryRepository;

    @Autowired
    private EvolutionRepository evolutionRepository;

    @Autowired
    private QueryRepository queryRepository;

    public MappingEntity create(MappingInit init) {
        final var category = categoryRepository.find(init.categoryId());
        final var newVersion = category.systemVersion().generateNext();
        final var mapping = MappingEntity.createNew(newVersion, init.categoryId(), init.datasourceId(), init.rootObjexKey(), init.primaryKey(), init.kindName(), init.accessPath());
        // FIXME Add some data to the evolution.
        final var evolution = MappingEvolution.createNew(category.id(), newVersion, mapping.id(), null);

        repository.save(mapping);
        evolutionRepository.create(evolution);

        propagateEvolution(category, evolution);

        return mapping;
    }

    public void update(MappingEntity mapping, MappingEdit edit) {
        final var category = categoryRepository.find(mapping.categoryId);

        final var newVersion = category.systemVersion().generateNext();
        final var evolution = MappingEvolution.createNew(category.id(), newVersion, mapping.id(), edit);

        mapping.updateVersion(newVersion, category.systemVersion());

        mapping.primaryKey = edit.primaryKey();
        mapping.kindName = edit.kindName();
        mapping.accessPath = edit.accessPath();

        repository.save(mapping);
        evolutionRepository.create(evolution);

        propagateEvolution(category, evolution);
    }

    // TODO Probably should be moved to dedicated service once we have orm.
    private void propagateEvolution(SchemaCategoryEntity category, MappingEvolution evolution) {
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
