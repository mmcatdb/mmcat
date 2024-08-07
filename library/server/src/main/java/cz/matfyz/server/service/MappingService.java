package cz.matfyz.server.service;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
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
        final Version systemVersion = projectRepository.getVersionByLogicalModel(init.logicalModelId());
        final Id generatedId = repository.add(init);

        return generatedId == null ? null : new MappingInfo(
            generatedId,
            init.kindName(),
            systemVersion
        );
    }

    /**
     * Created for the case when we receive Mapping from inference.
     */
    public MappingInfo createNew(Mapping mapping, Id logicalModelId) {
        final Version systemVersion = projectRepository.getVersionByLogicalModel(logicalModelId);
        Signature[] primaryKeyArray = mapping.primaryKey().toArray(new Signature[0]);

        final MappingInit init = new MappingInit(
            logicalModelId,
            mapping.rootObject().key(),
            primaryKeyArray,
            mapping.kindName(),
            mapping.accessPath()
        );

        Id generatedId = repository.add(init);
        if (generatedId != null) {
            return new MappingInfo(
                generatedId,
                mapping.kindName(),
                systemVersion
            );
        }
        else {
            return null;
        }
    }

}
