package cz.cuni.matfyz.server.service;

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

    public List<MappingWrapper> findAllInLogicalModel(int logicalModelId) {
        return repository.findAllInLogicalModel(logicalModelId);
    }

    public MappingWrapper find(int id) {
        return repository.find(id);
    }

    public MappingWrapper createNew(MappingInit wrapper) {
        Integer generatedId = repository.add(wrapper);

        return generatedId == null ? null : new MappingWrapper(
            generatedId,
            wrapper.logicalModelId(),
            wrapper.rootObjectId(),
            wrapper.rootMorphismId(),
            wrapper.mappingJsonValue(),
            wrapper.jsonValue()
        );
    }
}
