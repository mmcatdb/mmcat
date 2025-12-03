package cz.matfyz.server.adaptation;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.server.adaptation.Adaptation.AdaptationMorphism;
import cz.matfyz.server.adaptation.Adaptation.AdaptationObjex;
import cz.matfyz.server.adaptation.Adaptation.AdaptationSettings;
import cz.matfyz.server.category.SchemaCategoryRepository;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.mapping.MappingRepository;
import cz.matfyz.server.utils.entity.Id;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class AdaptationService {

    @Autowired
    private AdaptationRepository repository;

    @Autowired
    private SchemaCategoryRepository categoryRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private MappingRepository mappingRepository;

    public Adaptation createForCategory(Id categoryId) {
        final var categoryEntity = categoryRepository.find(categoryId);
        final var category = categoryEntity.toSchemaCategory();
        final var settings = createDefaultSettings(categoryId, category);

        final var adaptation = Adaptation.createNew(categoryEntity, settings);
        repository.save(adaptation);
        return adaptation;
    }

    private AdaptationSettings createDefaultSettings(Id categoryId, SchemaCategory category) {
        final Map<Id, Datasource> datasources = new TreeMap<>();
        datasourceRepository
            .findAllInCategory(categoryId)
            .forEach(entity -> {
                final var datasource = entity.toDatasource();
                datasources.put(entity.id(), datasource);
            });

        final var builder = new AdaptationCategoryBuilder();

        mappingRepository
            .findAllInCategory(categoryId)
            .forEach(entity -> {
                final var datasource = datasources.get(entity.datasourceId);
                final var mapping = entity.toMapping(datasource, category);
                builder.processMapping(mapping, entity.datasourceId);
            });

        builder.processRemainingMorphisms();

        return new AdaptationSettings(
            Math.sqrt(2),
            builder.objexes.values().stream().toList(),
            builder.morphisms.values().stream().toList()
        );
    }

    private class AdaptationCategoryBuilder {

        final Map<Key, AdaptationObjex> objexes = new TreeMap<>();
        final Map<BaseSignature, AdaptationMorphism> morphisms = new TreeMap<>();

        private Mapping mapping;
        private Id datasourceId;

        void processMapping(Mapping mapping, Id datasourceId) {
            this.mapping = mapping;
            this.datasourceId = datasourceId;
            final var rootKey = mapping.rootObjex().key();
            // The root objex has to be an entity.
            // The last mapping will overwrite previous ones for the same objexes and morphisms.
            // TODO find a better way how to handle this?
            objexes.put(rootKey, new AdaptationObjex(rootKey, datasourceId));
            processSubpaths(mapping.accessPath(), mapping.rootObjex());
        }

        private void processSubpaths(ComplexProperty parent, SchemaObjex parentObjex) {
            for (final var child : parent.subpaths()) {
                var childObjex = parentObjex;

                if (!child.signature().isEmpty()) {
                    final var path = mapping.category().getPath(child.signature());
                    processSchemaPath(path);
                    childObjex = path.to();
                }

                if (child instanceof ComplexProperty complex)
                    processSubpaths(complex, childObjex);
            }
        }

        /** Process all objexes (except the first one) and all morphisms on the path. */
        private void processSchemaPath(SchemaPath path) {
            // Outer objexes are already processed. So we have to check only the inner ones.
            // Also, all morphisms have to be processed to capture relationships between objexes.
            for (final var edge : path.edges()) {
                final var signature = edge.signature();
                // We don't want to allow embedding and inlining by default, but we have to if the morphism does this on the start.
                // TODO Not sure whether the embedding/inlining distinction is correct here.
                final var morphism = new AdaptationMorphism(signature, true, edge.isArray(), !edge.isArray());
                morphisms.put(signature, morphism);

                final var childObjex = edge.to();
                if (childObjex.isEntity())
                    objexes.put(childObjex.key(), new AdaptationObjex(childObjex.key(), datasourceId));
            }
        }

        public void processRemainingMorphisms() {
            // There are still morphisms between objexes that might have not been processed because they are not on any mapping path.
            for (final var objex : objexes.values()) {
                // We take only morphisms from because we want the direction to be consistent. And we only care about base signatures.
                for (final var morphism : mapping.category().getObjex(objex.key()).from()) {
                    final var signature = morphism.signature();
                    if (morphisms.containsKey(signature))
                        continue;

                    morphisms.put(signature, AdaptationMorphism.createDefault(signature));
                }
            }
        }

    }

}
