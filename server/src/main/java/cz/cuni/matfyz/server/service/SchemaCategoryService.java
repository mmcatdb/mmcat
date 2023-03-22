package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.builder.SchemaCategoryContext;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.evolution.SchemaCategoryUpdate;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.repository.SchemaCategoryRepository;

import java.util.List;

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
        if (!update.beforeVersion().equals(wrapper.version))
            return null;

        final var context = new SchemaCategoryContext();
        final var evolutionUpdate = update.toEvolution(context);
        final var originalCategory = wrapper.toSchemaCategory(context);

        final var result = evolutionUpdate.apply(originalCategory);
        if (!result.status)
            return null;

        context.setVersion(context.getVersion().generateNext());

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(result.data, context);

        if (!repository.update(newWrapper, update))
            return null;

        return newWrapper;
    }
    
}
