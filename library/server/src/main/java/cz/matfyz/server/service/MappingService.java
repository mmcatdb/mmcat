package cz.matfyz.server.service;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.ProjectRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MappingService {

    @Autowired
    private MappingRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    public MappingWrapper find(Id id) {
        return repository.find(id);
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        return repository.findAll(logicalModelId);
    }

    public List<MappingInfo> findAllInfos(Id logicalModelId) {
        return repository.findAllInfos(logicalModelId);
    }

    public MappingInfo createNew(MappingInit init) {
        final Version systemVersion = projectRepository
            .getVersionByLogicalModel(init.logicalModelId())
            .generateNext();

        final var wrapper = new MappingWrapper(null, init.logicalModelId(), init.rootObjectKey(), init.primaryKey(), init.kindName(), init.accessPath(), systemVersion);
        repository.add(wrapper);

        return MappingInfo.fromWrapper(wrapper);
    }

}
