package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.repository.MappingRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class MappingService {

    @Autowired
    private MappingRepository repository;

    public MappingWrapper find(Id id) {
        return repository.find(id);
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        return repository.findAll(logicalModelId);
    }

    public List<MappingInfo> findAllInfos(Id logicalModelId) {
        return repository.findAllInfos(logicalModelId);
    }

    public MappingInfo createNew(MappingInit wrapper) {
        Id generatedId = repository.add(wrapper);

        return generatedId == null ? null : new MappingInfo(
            generatedId,
            wrapper.jsonValue()
        );
    }
}
