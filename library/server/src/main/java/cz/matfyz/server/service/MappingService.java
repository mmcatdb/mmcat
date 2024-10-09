package cz.matfyz.server.service;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.ProjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MappingService {

    @Autowired
    private MappingRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    public MappingInfo createNew(MappingInit init) {
        final Version systemVersion = projectRepository
            .getVersionByLogicalModel(init.logicalModelId())
            .generateNext();

        final var wrapper = MappingWrapper.createNew(init.logicalModelId(), init.rootObjectKey(), init.primaryKey(), init.kindName(), init.accessPath(), systemVersion);
        repository.save(wrapper);

        return MappingInfo.fromWrapper(wrapper);
    }

}
