package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.cuni.matfyz.server.builder.CategoryBuilder;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.repository.SchemaCategoryRepository;
import cz.cuni.matfyz.server.utils.Position;

import java.util.List;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class SchemaCategoryService {

    @Autowired
    private SchemaCategoryRepository repository;

    public List<SchemaCategoryInfo> findAllInfos() {
        return repository.findAllInfos();
    }

    public SchemaCategoryInfo createNewInfo(SchemaCategoryInit init) {
        if (init.label() == null)
            return null;
        
        final var newWrapper = SchemaCategoryWrapper.createNew(init.label());
        final Id generatedId = repository.add(newWrapper);

        return generatedId == null ? null : new SchemaCategoryInfo(generatedId, init.label(), newWrapper.version);
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return repository.findInfo(new Id("" + id));
    }

    public SchemaCategoryWrapper find(Id id) {
        return repository.find(id);
    }

    public SchemaCategoryWrapper update(Id id, SchemaCategoryUpdate update) {
        final var wrapper = repository.find(id);
        if (!update.getBeforeVersion().equals(wrapper.version))
            return null;

        final var category = new CategoryBuilder()
            .setCategoryWrapper(wrapper)
            .build();

        final var result = update.apply(category);
        if (!result.status)
            return null;

        final var originalPositions = new TreeMap<Key, Position>();
        for (final var object : wrapper.objects)
            originalPositions.put(object.key(), object.position());

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(
            result.data,
            id,
            wrapper.version.generateNext(),
            originalPositions
        );

        if (!repository.update(newWrapper))
            return null;

        return newWrapper;

        
        /*
        var temporaryIdMap = new TreeMap<Integer, Id>();
        for (var object : update.objects()) {
            var generatedObjectId = objectRepository.add(object, id);
            if (generatedObjectId == null)
                return null;

            temporaryIdMap.put(object.temporaryId(), generatedObjectId);
        }

        for (var morphism : update.morphisms()) {
            var domId = morphism.domId() != null ? morphism.domId() : temporaryIdMap.get(morphism.temporaryDomId());
            var codId = morphism.codId() != null ? morphism.codId() : temporaryIdMap.get(morphism.temporaryCodId());
            var fixedMorphism = new SchemaMorphismUpdateFixed(domId, codId, morphism.jsonValue());

            var morphismResult = morphismRepository.add(fixedMorphism, id);

            if (morphismResult == null)
                return null;
        }

        return find(id);
         */
    }
}
