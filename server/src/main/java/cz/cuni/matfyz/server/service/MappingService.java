package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.mapping.MappingDetail;
import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.repository.MappingRepository;

import java.util.ArrayList;
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
    private SchemaObjectService objectService;

    public MappingDetail find(Id id) {
        final var mapping = repository.find(id);
        if (mapping == null)
            return null;

        final var rootObject = objectService.find(mapping.rootObjectId);
        if (rootObject == null)
            return null;
        
        return new MappingDetail(mapping, rootObject);
    }

    public List<MappingDetail> findAll(Id logicalModelId) {
        final var mappings = repository.findAll(logicalModelId);
        if (mappings == null)
            return null;
            
        final var objects = objectService.findAllInLogicalModel(logicalModelId);

        final var output = new ArrayList<MappingDetail>();
        for (final var mapping : mappings) {
            final var rootObject = objects.stream().filter(object -> object.id.equals(mapping.rootObjectId)).findFirst();
            if (!rootObject.isPresent())
                return null;
            
            output.add(new MappingDetail(mapping, rootObject.get()));
        }

        return output;
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
