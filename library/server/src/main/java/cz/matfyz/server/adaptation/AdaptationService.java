package cz.matfyz.server.adaptation;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.server.adaptation.Adaptation.AdaptationMapping;
import cz.matfyz.server.adaptation.Adaptation.AdaptationMorphism;
import cz.matfyz.server.adaptation.Adaptation.AdaptationObjex;
import cz.matfyz.server.adaptation.Adaptation.AdaptationSettings;
import cz.matfyz.server.category.SchemaCategoryRepository;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.mapping.MappingRepository;
import cz.matfyz.server.utils.entity.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

        final var builder = new AdaptationCategoryBuilder(category);

        mappingRepository
            .findAllInCategory(categoryId)
            .forEach(entity -> {
                final var datasource = datasources.get(entity.datasourceId);
                final var mapping = entity.toMapping(datasource, category);
                builder.addMapping(mapping);
            });

        builder.process();

        return new AdaptationSettings(
            Math.sqrt(2),
            builder.objexes.values().stream().toList(),
            builder.morphisms.values().stream().toList(),
            // By default, all datasources in the category are enabled.
            datasources.keySet().stream().toList()
        );
    }

    private class AdaptationCategoryBuilder {

        final SchemaCategory category;

        AdaptationCategoryBuilder(SchemaCategory category) {
            this.category = category;
        }

        final Map<Key, AdaptationObjex> objexes = new TreeMap<>();
        final Map<BaseSignature, AdaptationMorphism> morphisms = new TreeMap<>();

        private final List<Mapping> mappings = new ArrayList<>();


        // The point is to find all entities and assign datasources to them based on the mappings.
        // An entity is considered in a datasource if (a) is the root objex of a mapping in the datasource, or, if (b) is on some path in such mapping.
        // However, all (a) datasources have precedence over (b) datasources. The reason is that (b) ones might be there only to support relationships (and it's hard to distinguish between that and embedding).

        void addMapping(Mapping mapping) {
            mappings.add(mapping);
        }

        public void process() {
            for (final var mapping : mappings)
                processMappingRoot(mapping);

            // At this point, we already have all root entities. First, we add reference morphisms between them.
            for (final var objex : objexes.values()) {
                // We take only morphisms from because we want the direction to be consistent. And we only care about base signatures.
                for (final var morphism : category.getObjex(objex.key()).from()) {
                    if (!objexes.containsKey(morphism.cod().key()))
                        // Only morphisms between root objexes are considered here.
                        continue;

                    final var signature = morphism.signature();
                    if (!morphisms.containsKey(signature))
                        morphisms.put(signature, new AdaptationMorphism(signature, true, false));
                }
            }

            // Then, we process all inner entities collected from mapping paths.
            for (final var mapping : mappings)
                processMappingInner(mapping);

            // We don't indclude non-mapped entities nor morphisms.
        }

        private void processMappingRoot(Mapping mapping) {
            // The root objex has to be an entity.
            // The last mapping will overwrite previous ones for the same objexes and morphisms.
            // TODO find a better way how to handle this?
            final var rootKey = mapping.rootObjex().key();
            objexes.put(rootKey, new AdaptationObjex(rootKey, mockAdaptationMapping(mapping)));
        }

        private void processMappingInner(Mapping mapping) {
            this.mapping = mapping;
            processSubpaths(mapping.accessPath(), mapping.rootObjex());
        }

        private Mapping mapping;

        private void processSubpaths(ComplexProperty parent, SchemaObjex parentObjex) {
            for (final var child : parent.subpaths()) {
                var childObjex = parentObjex;

                if (!child.signature().isEmpty()) {
                    final var path = category.getPath(child.signature());
                    // Process all objexes (except the first one) and all morphisms on the path.
                    for (final var edge : path.edges())
                        processInnerEdge(edge, child);

                    childObjex = path.to();
                }

                if (child instanceof ComplexProperty complex)
                    processSubpaths(complex, childObjex);
            }
        }

        private void processInnerEdge(SchemaEdge edge, AccessPath child) {
            final var signature = edge.signature();
            final var childObjex = edge.to();
            if (!childObjex.isEntity())
                // We care only about entities.
                return;

            if (morphisms.containsKey(signature) || objexes.containsKey(childObjex.key()))
                // The intra-mapping morphisms have precedence.
                return;

            final var idSignatures = childObjex.ids().collectAllSignatures();
            final var nonKeyProperties = childObjex.from().stream()
                .filter(morphism -> !idSignatures.contains(morphism.signature()))
                .map(SchemaMorphism::cod)
                .collect(Collectors.toSet());

            if (!shouldIncludeEntity(child, nonKeyProperties))
                return;

            // We don't want to allow embedding by default, but we have to do if the morphism uses this on the start.
            morphisms.put(signature, new AdaptationMorphism(signature, true, edge.isArray()));
            objexes.put(childObjex.key(), new AdaptationObjex(childObjex.key(), mockAdaptationMapping(mapping)));
        }

        private boolean shouldIncludeEntity(AccessPath property, Set<SchemaObjex> nonKeyProperties) {
            // This is NOT ideal but what can we do ...
            // Let's say that an entity is included in a mapping if there is at least one of its non-key properties.
            if (!(property instanceof ComplexProperty complex)) {
                final var propertyObjex = category.getPath(property.signature()).to();
                return nonKeyProperties.contains(propertyObjex);
            }

            for (final var subpath : complex.subpaths()) {
                if (shouldIncludeEntity(subpath, nonKeyProperties))
                    return true;
            }

            return false;
        }

        /** @deprecated */
        private static AdaptationMapping mockAdaptationMapping(Mapping mapping) {
            // TODO Use fix values for each objex/datasource pair.
            // FIXME Use real values.
            final var dataSizeInBytes = Math.round(Math.random() * 29483553);
            final var recordCount = Math.round(dataSizeInBytes / 150);
            return new AdaptationMapping(new Id(mapping.datasource().identifier), dataSizeInBytes, recordCount);
        }

    }

}
