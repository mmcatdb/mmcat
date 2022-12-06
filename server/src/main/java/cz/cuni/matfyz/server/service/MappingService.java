package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.mapping.MappingFull;
import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.repository.LogicalModelRepository;
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

    @Autowired
    private LogicalModelRepository logicalModelRepository;

    public List<MappingWrapper> findAllWrappers(int logicalModelId) {
        return repository.findAllWrappers(logicalModelId);
    }

    public MappingWrapper find(int id) {
        return repository.find(id);
    }

    public List<MappingInfo> findAllInfos(int logicalModelId) {
        return repository.findAllInfos(logicalModelId);
    }

    public MappingFull findFull(int id) {
        var wrapper = repository.find(id);
        var logicalModel = logicalModelRepository.find(wrapper.logicalModelId);

        return new MappingFull(
            wrapper.id,
            logicalModel.toInfo(),
            wrapper.rootObjectId,
            wrapper.mappingJsonValue,
            wrapper.jsonValue
        );
    }

    public List<MappingFull> findAllFull(int logicalModelId) {
        var logicalModel = logicalModelRepository.find(logicalModelId);

        return repository.findAllWrappers(logicalModelId).stream().map(wrapper -> new MappingFull(
            wrapper.id,
            logicalModel.toInfo(),
            wrapper.rootObjectId,
            wrapper.mappingJsonValue,
            wrapper.jsonValue
        )).toList();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public List<MappingFull> findAllFullInCategory(int categoryId) {
        var logicalModels = logicalModelRepository.findAllInCategory(categoryId);

        return logicalModels.stream().flatMap(logicalModel -> {
            return repository.findAllWrappers(logicalModel.id).stream().map(wrapper -> new MappingFull(
                wrapper.id,
                logicalModel.toInfo(),
                wrapper.rootObjectId,
                wrapper.mappingJsonValue,
                wrapper.jsonValue
            ));
        }).toList();
    }

    public MappingInfo createNew(MappingInit wrapper) {
        Integer generatedId = repository.add(wrapper);

        return generatedId == null ? null : new MappingInfo(
            generatedId,
            wrapper.jsonValue()
        );
    }
}
