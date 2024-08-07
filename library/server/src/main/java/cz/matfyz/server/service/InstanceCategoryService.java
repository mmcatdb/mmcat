package cz.matfyz.server.service;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.instance.InstanceCategoryWrapper;
import cz.matfyz.server.repository.InstanceCategoryRepository;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceCategoryService {

    @Autowired
    private InstanceCategoryRepository repository;

    public @Nullable InstanceCategoryWrapper findCategory(Id sessionId) {
        return repository.find(sessionId);
    }

    public @Nullable InstanceCategory loadCategory(Id sessionId, SchemaCategory schemaCategory) {
        final var wrapper = repository.find(sessionId);
        //if (wrapper == null) {System.out.println("wrapper is null in InstanceCatService.loadCategory()");}
        return wrapper != null ? wrapper.toInstanceCategory(schemaCategory) : null;
    }

    public InstanceCategoryWrapper saveCategory(Id sessionId, Id categoryId, InstanceCategory category) {
        final var wrapper = InstanceCategoryWrapper.fromInstanceCategory(sessionId, categoryId, category);
        repository.save(wrapper);

        return wrapper;
    }

}
