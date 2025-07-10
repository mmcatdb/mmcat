package cz.matfyz.server.service;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.InstanceCategoryEntity;
import cz.matfyz.server.repository.InstanceCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceCategoryService {

    @Autowired
    private InstanceCategoryRepository repository;

    public InstanceCategoryEntity saveCategory(Id sessionId, Id categoryId, InstanceCategory category) {
        final var entity = InstanceCategoryEntity.fromInstanceCategory(sessionId, categoryId, category);
        repository.save(entity);

        return entity;
    }

}
