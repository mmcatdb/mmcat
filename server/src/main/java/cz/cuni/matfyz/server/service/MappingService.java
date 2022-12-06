package cz.cuni.matfyz.server.service;

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

    public MappingWrapper find(int id) {
        return repository.find(id);
    }

    public List<MappingWrapper> findAll(int logicalModelId) {
        return repository.findAll(logicalModelId);
    }

    public List<MappingInfo> findAllInfos(int logicalModelId) {
        return repository.findAllInfos(logicalModelId);
    }

    public MappingInfo createNew(MappingInit wrapper) {
        Integer generatedId = repository.add(wrapper);

        return generatedId == null ? null : new MappingInfo(
            generatedId,
            wrapper.jsonValue()
        );
    }
}
